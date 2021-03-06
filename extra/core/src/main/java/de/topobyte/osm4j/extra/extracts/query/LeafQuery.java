// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.extra.extracts.query;

import java.io.IOException;
import java.nio.file.Path;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;
import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.jts.utils.GeometryGroup;
import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class LeafQuery extends AbstractQuery
{

	final static Logger logger = LoggerFactory.getLogger(LeafQuery.class);

	private PredicateEvaluator test;

	private DataTreeFiles filesTreeNodes;
	private DataTreeFiles filesTreeWays;
	private DataTreeFiles filesTreeSimpleRelations;
	private DataTreeFiles filesTreeComplexRelations;

	private boolean fastRelationTests;

	public LeafQuery(PredicateEvaluator test, DataTreeFiles filesTreeNodes,
			DataTreeFiles filesTreeWays, DataTreeFiles filesTreeSimpleRelations,
			DataTreeFiles filesTreeComplexRelations, FileFormat inputFormat,
			OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig, boolean fastRelationsTests)
	{
		super(inputFormat, outputConfigIntermediate, outputConfig);

		this.test = test;
		this.filesTreeNodes = filesTreeNodes;
		this.filesTreeWays = filesTreeWays;
		this.filesTreeSimpleRelations = filesTreeSimpleRelations;
		this.filesTreeComplexRelations = filesTreeComplexRelations;
		this.fastRelationTests = fastRelationsTests;
	}

	private Path pathOutNodes;
	private Path pathOutWays;
	private Path pathOutAdditionalNodes;
	private Path pathOutAdditionalWays;
	private Path pathOutSimpleRelations;
	private Path pathOutComplexRelations;

	private OsmStreamOutput outNodes;
	private OsmStreamOutput outWays;
	private OsmStreamOutput outSimpleRelations;
	private OsmStreamOutput outComplexRelations;

	private InMemoryListDataSet dataNodes;
	private InMemoryListDataSet dataWays;
	private InMemoryListDataSet dataSimpleRelations;
	private InMemoryListDataSet dataComplexRelations;

	private TLongSet nodeIds = new TLongHashSet();
	private TLongSet wayIds = new TLongHashSet();

	private TLongObjectMap<OsmNode> additionalNodes = new TLongObjectHashMap<>();
	private TLongObjectMap<OsmWay> additionalWays = new TLongObjectHashMap<>();

	private GeometryFactory factory = new GeometryFactory();
	private WayBuilder wayBuilder = new WayBuilder(factory);

	public QueryResult execute(Node leaf, Path pathOutNodes, Path pathOutWays,
			Path pathOutSimpleRelations, Path pathOutComplexRelations,
			Path pathOutAdditionalNodes, Path pathOutAdditionalWays,
			RelationGroupFilter relationGroupFilter) throws IOException
	{
		this.pathOutNodes = pathOutNodes;
		this.pathOutWays = pathOutWays;
		this.pathOutSimpleRelations = pathOutSimpleRelations;
		this.pathOutComplexRelations = pathOutComplexRelations;
		this.pathOutAdditionalNodes = pathOutAdditionalNodes;
		this.pathOutAdditionalWays = pathOutAdditionalWays;

		logger.info("loading data");
		readData(leaf);

		createOutputs();

		logger.info("querying nodes");
		queryNodes();

		logger.info("querying ways");
		queryWays();

		logger.info("querying simple relations");
		RelationQueryBag queryBagSimple = new RelationQueryBag(
				outSimpleRelations, additionalNodes, additionalWays, nodeIds,
				wayIds);

		SimpleRelationsQuery simpleRelationsQuery = new SimpleRelationsQuery(
				dataNodes, dataWays, dataSimpleRelations, test,
				fastRelationTests);
		simpleRelationsQuery.execute(queryBagSimple);

		logger.info("querying complex relations");
		RelationQueryBag queryBagComplex = new RelationQueryBag(
				outComplexRelations, additionalNodes, additionalWays, nodeIds,
				wayIds);

		ComplexRelationsQuery complexRelationsQuery = new ComplexRelationsQuery(
				dataNodes, dataWays, dataComplexRelations, test,
				fastRelationTests, relationGroupFilter);
		complexRelationsQuery.execute(queryBagComplex);

		logger.info("writing additional nodes");
		writeAdditionalNodes();

		logger.info("writing additional ways");
		writeAdditionalWays();

		logger.info("closing output");
		finishOutputs();

		return new QueryResult(nodeIds.size(), wayIds.size(),
				queryBagSimple.nSimple, queryBagComplex.nComplex);
	}

	private void createOutputs() throws IOException
	{
		outNodes = createOutput(pathOutNodes);
		outWays = createOutput(pathOutWays);
		outSimpleRelations = createOutput(pathOutSimpleRelations);
		outComplexRelations = createOutput(pathOutComplexRelations);
	}

	private void finishOutputs() throws IOException
	{
		finish(outNodes);
		finish(outWays);
		finish(outSimpleRelations);
		finish(outComplexRelations);
	}

	private void readData(Node leaf) throws IOException
	{
		dataNodes = read(filesTreeNodes.getPath(leaf));
		dataWays = read(filesTreeWays.getPath(leaf));
		dataSimpleRelations = read(filesTreeSimpleRelations.getPath(leaf));
		dataComplexRelations = read(filesTreeComplexRelations.getPath(leaf));
	}

	private void queryNodes() throws IOException
	{
		for (OsmNode node : dataNodes.getNodes()) {
			if (test.contains(
					new Coordinate(node.getLongitude(), node.getLatitude()))) {
				nodeIds.add(node.getId());
				outNodes.getOsmOutput().write(node);
			}
		}
	}

	private void queryWays() throws IOException
	{
		for (OsmWay way : dataWays.getWays()) {
			boolean in = QueryUtil.anyNodeContainedIn(way, nodeIds);
			if (!in && way.getNumberOfNodes() > 1) {
				try {
					WayBuilderResult result = wayBuilder.build(way, dataNodes);
					GeometryGroup group = result.toGeometryGroup(factory);
					if (test.intersects(group)) {
						in = true;
					}
				} catch (EntityNotFoundException e) {
					logger.warn("Unable to build way: " + way.getId());
				}
			}
			if (!in) {
				continue;
			}
			wayIds.add(way.getId());
			outWays.getOsmOutput().write(way);
			try {
				QueryUtil.putNodes(way, additionalNodes, dataNodes, nodeIds);
			} catch (EntityNotFoundException e) {
				logger.warn("Unable to find all nodes for way: " + way.getId());
			}
		}
	}

	private void writeAdditionalNodes() throws IOException
	{
		OsmStreamOutput output = createOutput(pathOutAdditionalNodes);
		QueryUtil.writeNodes(additionalNodes, output.getOsmOutput());
		finish(output);
	}

	private void writeAdditionalWays() throws IOException
	{
		OsmStreamOutput output = createOutput(pathOutAdditionalWays);
		QueryUtil.writeWays(additionalWays, output.getOsmOutput());
		finish(output);
	}

}

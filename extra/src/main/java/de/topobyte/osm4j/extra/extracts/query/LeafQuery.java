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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.GeometryGroup;
import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class LeafQuery
{

	private PredicateEvaluator test;

	private DataTreeFiles filesTreeNodes;
	private DataTreeFiles filesTreeWays;
	private DataTreeFiles filesTreeSimpleRelations;
	private DataTreeFiles filesTreeComplexRelations;

	private FileFormat inputFormat;
	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	private boolean fastRelationTests;

	public LeafQuery(PredicateEvaluator test, DataTreeFiles filesTreeNodes,
			DataTreeFiles filesTreeWays,
			DataTreeFiles filesTreeSimpleRelations,
			DataTreeFiles filesTreeComplexRelations, FileFormat inputFormat,
			FileFormat outputFormat, boolean writeMetadata,
			PbfConfig pbfConfig, TboConfig tboConfig, boolean fastRelationsTests)
	{
		this.test = test;
		this.filesTreeNodes = filesTreeNodes;
		this.filesTreeWays = filesTreeWays;
		this.filesTreeSimpleRelations = filesTreeSimpleRelations;
		this.filesTreeComplexRelations = filesTreeComplexRelations;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
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
			Path pathOutAdditionalNodes, Path pathOutAdditionalWays)
			throws IOException
	{
		this.pathOutNodes = pathOutNodes;
		this.pathOutWays = pathOutWays;
		this.pathOutSimpleRelations = pathOutSimpleRelations;
		this.pathOutComplexRelations = pathOutComplexRelations;
		this.pathOutAdditionalNodes = pathOutAdditionalNodes;
		this.pathOutAdditionalWays = pathOutAdditionalWays;

		System.out.println("loading data");
		readData(leaf);

		createOutputs();

		System.out.println("querying nodes");
		queryNodes();

		System.out.println("querying ways");
		queryWays();

		System.out.println("querying simple relations");
		RelationQueryBag queryBagSimple = new RelationQueryBag(
				outSimpleRelations, additionalNodes, additionalWays, nodeIds,
				wayIds);

		SimpleRelationsQuery simpleRelationsQuery = new SimpleRelationsQuery(
				dataNodes, dataWays, dataSimpleRelations, test,
				fastRelationTests);
		simpleRelationsQuery.execute(queryBagSimple);

		System.out.println("querying complex relations");
		RelationQueryBag queryBagComplex = new RelationQueryBag(
				outComplexRelations, additionalNodes, additionalWays, nodeIds,
				wayIds);

		ComplexRelationsQuery complexRelationsQuery = new ComplexRelationsQuery(
				dataNodes, dataWays, dataComplexRelations, test,
				fastRelationTests);
		complexRelationsQuery.execute(queryBagComplex);

		System.out.println("writing additional nodes");
		writeAdditionalNodes();

		System.out.println("writing additional ways");
		writeAdditionalWays();

		System.out.println("closing output");
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

	private InMemoryListDataSet read(Path path) throws IOException
	{
		OsmFileInput fileInput = new OsmFileInput(path, inputFormat);
		OsmIteratorInput input = fileInput.createIterator(true, writeMetadata);
		InMemoryListDataSet data = ListDataSetLoader.read(input.getIterator(),
				true, true, true);
		input.close();
		return data;
	}

	private OsmStreamOutput createOutput(Path path) throws IOException
	{
		OutputStream outputStream = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutputStream = OsmIoUtils
				.setupOsmOutput(outputStream, outputFormat, writeMetadata,
						pbfConfig, tboConfig);
		return new OsmOutputStreamStreamOutput(outputStream, osmOutputStream);
	}

	private void finish(OsmStreamOutput osmOutput) throws IOException
	{
		osmOutput.getOsmOutput().complete();
		osmOutput.close();
	}

	private void queryNodes() throws IOException
	{
		for (OsmNode node : dataNodes.getNodes()) {
			if (test.contains(new Coordinate(node.getLongitude(), node
					.getLatitude()))) {
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
					System.out.println("Unable to build way: " + way.getId());
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
				System.out.println("Unable to find all nodes for way: "
						+ way.getId());
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

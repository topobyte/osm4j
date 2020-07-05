// Copyright 2020 Sebastian Kuerten
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
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.extracts.BatchFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionPaths;
import de.topobyte.osm4j.extra.extracts.TreeFileNames;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class BaseQuery extends AbstractQuery
{

	final static Logger logger = LoggerFactory.getLogger(BaseQuery.class);

	protected GeometryFactory factory = new GeometryFactory();

	protected ExtractionPaths paths;
	protected TreeFileNames treeNames;
	protected BatchFileNames relationNames;

	protected DataTree tree;
	protected DataTreeFiles filesTreeNodes;
	protected DataTreeFiles filesTreeWays;
	protected DataTreeFiles filesTreeSimpleRelations;
	protected DataTreeFiles filesTreeComplexRelations;

	protected boolean keepTmp;
	protected boolean fastRelationTests;

	public BaseQuery(ExtractionPaths paths, TreeFileNames treeNames,
			BatchFileNames relationNames, FileFormat inputFormat,
			OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig, boolean keepTmp,
			boolean fastRelationTests)
	{
		super(inputFormat, outputConfigIntermediate, outputConfig);

		this.paths = paths;
		this.treeNames = treeNames;
		this.relationNames = relationNames;
		this.keepTmp = keepTmp;
		this.fastRelationTests = fastRelationTests;
	}

	protected void openTree() throws IOException
	{
		Path pathTree = paths.getTree();
		tree = DataTreeOpener.open(pathTree);

		filesTreeNodes = new DataTreeFiles(pathTree, treeNames.getNodes());
		filesTreeWays = new DataTreeFiles(pathTree, treeNames.getWays());
		filesTreeSimpleRelations = new DataTreeFiles(pathTree,
				treeNames.getSimpleRelations());
		filesTreeComplexRelations = new DataTreeFiles(pathTree,
				treeNames.getComplexRelations());
	}

	protected OsmFileInput input(Path path)
	{
		return new OsmFileInput(path, inputFormat);
	}

	protected OsmFileInput intermediate(Path path)
	{
		return new OsmFileInput(path, outputConfigIntermediate.getFileFormat());
	}

	protected void addCompletelyContainedLeaf(IntermediateFiles files,
			Node leaf)
	{
		files.getFilesNodes().add(input(filesTreeNodes.getPath(leaf)));
		files.getFilesWays().add(input(filesTreeWays.getPath(leaf)));
		files.getFilesSimpleRelations()
				.add(input(filesTreeSimpleRelations.getPath(leaf)));
		files.getFilesComplexRelations()
				.add(input(filesTreeComplexRelations.getPath(leaf)));
	}

	protected void addCompletelyContainedBatch(IntermediateFiles files,
			Path pathRelations, long id, List<OsmFileInput> filesRelations)
	{
		Path path = pathRelations.resolve(Long.toString(id));
		files.getFilesNodes()
				.add(input(path.resolve(relationNames.getNodes())));
		files.getFilesWays().add(input(path.resolve(relationNames.getWays())));
		filesRelations.add(input(path.resolve(relationNames.getRelations())));
	}

	class BatchDataSet
	{

		InMemoryListDataSet dataRelations;
		InMemoryListDataSet selectedRelations;
		InMemoryListDataSet dataNodes;
		InMemoryListDataSet dataWays;

		public boolean load(IdBboxEntry entry, Path pathBatch,
				RelationFilter relationFilter) throws IOException
		{
			Path pathDir = pathBatch.resolve(Long.toString(entry.getId()));
			Path pathNodes = pathDir.resolve(relationNames.getNodes());
			Path pathWays = pathDir.resolve(relationNames.getWays());
			Path pathRelations = pathDir.resolve(relationNames.getRelations());

			logger.info("loading data");
			dataRelations = read(pathRelations);

			dataRelations.sort();
			if (relationFilter == null) {
				selectedRelations = dataRelations;
			} else {
				selectedRelations = new RelationSelector()
						.select(relationFilter, dataRelations);
				selectedRelations.sort();

				logger.info(String.format("selected %d of %d relations",
						selectedRelations.getRelations().size(),
						dataRelations.getRelations().size()));
			}

			if (selectedRelations.getRelations().isEmpty()) {
				logger.info("nothing selected, skipping");
				return false;
			}

			dataNodes = read(pathNodes);
			dataWays = read(pathWays);

			return true;
		}

	}

	/*
	 * This is run on each batch of relations
	 */
	protected void runRelationsQuery(IntermediateFiles files,
			PredicateEvaluator test, boolean simple, BatchDataSet data,
			Path pathOutNodes, Path pathOutWays, Path pathOutRelations)
			throws IOException
	{
		OsmStreamOutput outRelations = createOutput(pathOutRelations);
		RelationQueryBag queryBag = new RelationQueryBag(outRelations);

		logger.info("running query");
		// First determine all nodes of this batch that are within the
		// requested region for quick relation selection by member id
		QueryUtil.queryNodes(test, data.dataNodes, queryBag.nodeIds);
		// Also determine all ways that reference any of the nodes selected
		// before, also for quick relation selection by member id
		QueryUtil.queryWays(data.dataWays, queryBag.nodeIds, queryBag.wayIds);

		if (simple) {
			SimpleRelationsQuery simpleRelationsQuery = new SimpleRelationsQuery(
					data.dataNodes, data.dataWays, data.selectedRelations, test,
					fastRelationTests);
			simpleRelationsQuery.execute(queryBag);
		} else {
			ComplexRelationsQuery complexRelationsQuery = new ComplexRelationsQuery(
					data.dataNodes, data.dataWays, data.selectedRelations, test,
					fastRelationTests);
			complexRelationsQuery.execute(queryBag);
		}

		finish(outRelations);

		logger.info("writing nodes and ways");
		OsmStreamOutput outputNodes = createOutput(pathOutNodes);
		QueryUtil.writeNodes(queryBag.additionalNodes,
				outputNodes.getOsmOutput());
		finish(outputNodes);

		OsmStreamOutput outputWays = createOutput(pathOutWays);
		QueryUtil.writeWays(queryBag.additionalWays, outputWays.getOsmOutput());
		finish(outputWays);

		files.getFilesNodes().add(intermediate(pathOutNodes));
		files.getFilesWays().add(intermediate(pathOutWays));
		files.getFilesSimpleRelations().add(intermediate(pathOutRelations));
	}

}

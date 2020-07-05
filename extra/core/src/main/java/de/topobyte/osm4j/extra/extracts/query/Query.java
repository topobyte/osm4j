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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.extracts.BatchFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionPaths;
import de.topobyte.osm4j.extra.extracts.TreeFileNames;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxUtil;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class Query extends BaseQuery
{

	final static Logger logger = LoggerFactory.getLogger(Query.class);

	private Envelope queryEnvelope;
	private PredicateEvaluator test;

	private Path pathOutput;
	private Path pathTmp;

	private RelationFilter relationFilter;

	/**
	 * Create a query to extract data contained in an area from an extraction
	 * database.
	 * 
	 * @param queryEnvelope
	 *            the bounding envelope of the region to extract.
	 * @param test
	 *            a PredicateEvaluator used for determining inclusion in the
	 *            extract.
	 * @param pathOutput
	 *            a path to a file to store output data in.
	 * @param pathTmp
	 *            a directory to store intermediate, temporary files (pass null
	 *            to use the system's default temporary storage, i.e.
	 *            {@link Files#createTempDirectory(String, FileAttribute...)}
	 *            will be used.)
	 * @param paths
	 *            an ExtractionPaths object configured for an extraction
	 *            database.
	 * @param treeNames
	 *            the names of the files in the data tree.
	 * @param relationNames
	 *            the names of the files in the relation batches.
	 * @param inputFormat
	 *            the {@link FileFormat} of the database extract files.
	 * @param outputConfigIntermediate
	 *            configuration for intermediate file storage
	 * @param outputConfig
	 *            configuration for the final output file.
	 * @param keepTmp
	 *            whether to keep temporary files after the extraction is done.
	 * @param fastRelationTests
	 *            whether to include relations based on their bounding box (and
	 *            not by evaluating their exact geometry).
	 * @param relationFilter
	 *            a filter to select a subset of relations with. Pass null to
	 *            select all relations. If only a subset of relations is
	 *            selected, all transitively referenced relations will be
	 *            included as well.
	 */
	public Query(Envelope queryEnvelope, PredicateEvaluator test,
			Path pathOutput, Path pathTmp, ExtractionPaths paths,
			TreeFileNames treeNames, BatchFileNames relationNames,
			FileFormat inputFormat, OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig, boolean keepTmp,
			boolean fastRelationTests, RelationFilter relationFilter)
	{
		super(paths, treeNames, relationNames, inputFormat,
				outputConfigIntermediate, outputConfig, keepTmp,
				fastRelationTests);

		this.queryEnvelope = queryEnvelope;
		this.test = test;
		this.pathOutput = pathOutput;
		this.pathTmp = pathTmp;
		this.keepTmp = keepTmp;
		this.fastRelationTests = fastRelationTests;
		this.relationFilter = relationFilter;
	}

	private Path pathTmpTree;
	private Path pathTmpRelations;

	// Lists of files that need to be merged in the end
	private IntermediateFiles files = new IntermediateFiles();

	private int nNodes = 0;
	private int nWays = 0;
	private int nSimpleRelations = 0;
	private int nComplexRelations = 0;

	private int tmpIndexTree = 0;

	public void execute() throws IOException
	{
		createTemporaryDirectory();

		// Query setup

		openTree();

		// Query data tree

		queryTreeData();

		// Query relations

		queryRelations();

		// Merge intermediate files

		mergeFiles();

		// Delete intermediate files

		if (!keepTmp) {
			FileUtils.deleteDirectory(pathTmp.toFile());
		}
	}

	private void createTemporaryDirectory() throws IOException
	{
		// Make sure a temporary directory is available

		if (pathTmp == null) {
			pathTmp = Files.createTempDirectory("extract");
		}
		logger.info("Temporary directory: " + pathTmp);
		Files.createDirectories(pathTmp);
		if (!Files.isDirectory(pathTmp)) {
			String error = "Unable to create temporary directory for intermediate files";
			logger.error(error);
			throw new IOException(error);
		}
		if (pathTmp.toFile().listFiles().length != 0) {
			String error = "Temporary directory for intermediate files is not empty";
			logger.error(error);
			throw new IOException(error);
		}
		logger.info("Storing intermediate files here: " + pathTmp);

		// Create sub-directories for intermediate files

		pathTmpTree = pathTmp.resolve("tree");
		pathTmpRelations = pathTmp.resolve("relations");

		Files.createDirectory(pathTmpTree);
		Files.createDirectory(pathTmpRelations);
	}

	private void queryTreeData() throws IOException
	{
		Geometry box = factory.toGeometry(queryEnvelope);
		List<Node> leafs = tree.query(box);

		for (Node leaf : leafs) {
			String leafName = Long.toHexString(leaf.getPath());

			if (test.contains(leaf.getEnvelope())) {
				logger.info("Leaf is completely contained: " + leafName);
				addCompletelyContainedLeaf(files, leaf);
				continue;
			}

			logger.info("Loading data from leaf: " + leafName);
			addIntersectingLeaf(leaf);
		}

		logger.info(String.format("Total number of nodes: %d", nNodes));
		logger.info(String.format("Total number of ways: %d", nWays));
		logger.info(String.format("Total number of simple relations: %d",
				nSimpleRelations));
		logger.info(String.format("Total number of complex relations: %d",
				nComplexRelations));
	}

	private void queryRelations() throws IOException
	{
		List<IdBboxEntry> entriesSimple = IdBboxUtil
				.read(paths.getSimpleRelationsBboxes());
		List<IdBboxEntry> entriesComplex = IdBboxUtil
				.read(paths.getComplexRelationsBboxes());

		queryRelationBatches(entriesSimple, true, "Simple",
				paths.getSimpleRelations(), pathTmpRelations);
		queryRelationBatches(entriesComplex, false, "Complex",
				paths.getComplexRelations(), pathTmpRelations);
	}

	private void queryRelationBatches(List<IdBboxEntry> entries, boolean simple,
			String type, Path pathRelationBatches, Path pathOutput)
			throws IOException
	{
		int tmpIndex = 0;

		String lowerType = type.toLowerCase();
		String prefix = lowerType;

		List<OsmFileInput> filesRelations = simple
				? files.getFilesSimpleRelations()
				: files.getFilesComplexRelations();

		for (IdBboxEntry entry : entries) {
			long id = entry.getId();
			if (test.contains(entry.getEnvelope())) {
				logger.info(type + " batch completely contained: " + id);
				addCompletelyContainedBatch(files, pathRelationBatches, id,
						filesRelations);
			} else if (test.intersects(entry.getEnvelope())) {
				logger.info("Loading data from " + lowerType + " batch: " + id);

				BatchDataSet data = new BatchDataSet();
				boolean use = data.load(entry, pathRelationBatches,
						relationFilter);
				if (!use) {
					continue;
				}

				tmpIndex++;

				Path pathOutNodes = pathOutput
						.resolve(filename(prefix, EntityType.Node, tmpIndex));
				Path pathOutWays = pathOutput
						.resolve(filename(prefix, EntityType.Way, tmpIndex));
				Path pathOutRelations = pathOutput.resolve(
						filename(prefix, EntityType.Relation, tmpIndex));

				runRelationsQuery(files, test, simple, data, pathOutNodes,
						pathOutWays, pathOutRelations);
			}
		}
	}

	private void mergeFiles() throws IOException
	{
		OsmStreamOutput output = createFinalOutput(pathOutput);
		ExtractionUtil.merge(output, files, outputConfig);
	}

	private void addIntersectingLeaf(Node leaf) throws IOException
	{
		LeafQuery leafQuery = new LeafQuery(test, filesTreeNodes, filesTreeWays,
				filesTreeSimpleRelations, filesTreeComplexRelations,
				inputFormat, outputConfigIntermediate, outputConfig,
				fastRelationTests);

		tmpIndexTree++;

		Path pathOutNodes = pathTmpTree
				.resolve(filename(EntityType.Node, tmpIndexTree));
		Path pathOutWays = pathTmpTree
				.resolve(filename(EntityType.Way, tmpIndexTree));
		Path pathOutSimpleRelations = pathTmpTree
				.resolve(filename("simple", tmpIndexTree));
		Path pathOutComplexRelations = pathTmpTree
				.resolve(filename("complex", tmpIndexTree));
		Path pathOutAdditionalNodes = pathTmpTree
				.resolve(filename("nodes-extra", tmpIndexTree));
		Path pathOutAdditionalWays = pathTmpTree
				.resolve(filename("ways-extra", tmpIndexTree));

		QueryResult results = leafQuery.execute(leaf, pathOutNodes, pathOutWays,
				pathOutSimpleRelations, pathOutComplexRelations,
				pathOutAdditionalNodes, pathOutAdditionalWays);

		nNodes += results.getNumNodes();
		nWays += results.getNumWays();
		nSimpleRelations += results.getNumSimpleRelations();
		nComplexRelations += results.getNumComplexRelations();

		files.getFilesNodes().add(intermediate(pathOutNodes));
		files.getFilesNodes().add(intermediate(pathOutAdditionalNodes));
		files.getFilesWays().add(intermediate(pathOutWays));
		files.getFilesWays().add(intermediate(pathOutAdditionalWays));
		files.getFilesSimpleRelations()
				.add(intermediate(pathOutSimpleRelations));
		files.getFilesComplexRelations()
				.add(intermediate(pathOutComplexRelations));

		logger.info(String.format("Found %d nodes", results.getNumNodes()));
		logger.info(String.format("Found %d ways", results.getNumWays()));
		logger.info(String.format("Found %d simple relations",
				results.getNumSimpleRelations()));
		logger.info(String.format("Found %d complex relations",
				results.getNumComplexRelations()));
	}

}

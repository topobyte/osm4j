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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxUtil;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.merge.sorted.SortedMerge;

public class Query extends AbstractQuery
{

	private Path pathOutput;
	private Path pathTmp;
	private Path pathTree;
	private Path pathSimpleRelations;
	private Path pathComplexRelations;
	private Path pathSimpleRelationsBboxes;
	private Path pathComplexRelationsBboxes;

	private String fileNamesTreeNodes;
	private String fileNamesTreeWays;
	private String fileNamesTreeSimpleRelations;
	private String fileNamesTreeComplexRelations;
	private String fileNamesRelationNodes;
	private String fileNamesRelationWays;
	private String fileNamesRelationRelations;

	private Envelope queryEnvelope;
	private PredicateEvaluator test;

	private boolean keepTmp;

	private boolean fastRelationTests;

	public Query(Path pathOutput, Path pathTmp, Path pathTree,
			Path pathSimpleRelations, Path pathComplexRelations,
			Path pathSimpleRelationsBboxes, Path pathComplexRelationsBboxes,
			String fileNamesTreeNodes, String fileNamesTreeWays,
			String fileNamesTreeSimpleRelations,
			String fileNamesTreeComplexRelations,
			String fileNamesRelationNodes, String fileNamesRelationWays,
			String fileNamesRelationRelations, Envelope queryEnvelope,
			PredicateEvaluator test, FileFormat inputFormat,
			FileFormat outputFormat, boolean writeMetadata,
			PbfConfig pbfConfig, TboConfig tboConfig, boolean keepTmp,
			boolean fastRelationTests)
	{
		super(inputFormat, outputFormat, writeMetadata, pbfConfig, tboConfig);

		this.pathOutput = pathOutput;
		this.pathTmp = pathTmp;
		this.pathTree = pathTree;
		this.pathSimpleRelations = pathSimpleRelations;
		this.pathComplexRelations = pathComplexRelations;
		this.pathSimpleRelationsBboxes = pathSimpleRelationsBboxes;
		this.pathComplexRelationsBboxes = pathComplexRelationsBboxes;
		this.fileNamesTreeNodes = fileNamesTreeNodes;
		this.fileNamesTreeWays = fileNamesTreeWays;
		this.fileNamesTreeSimpleRelations = fileNamesTreeSimpleRelations;
		this.fileNamesTreeComplexRelations = fileNamesTreeComplexRelations;
		this.fileNamesRelationNodes = fileNamesRelationNodes;
		this.fileNamesRelationWays = fileNamesRelationWays;
		this.fileNamesRelationRelations = fileNamesRelationRelations;
		this.queryEnvelope = queryEnvelope;
		this.test = test;
		this.keepTmp = keepTmp;
		this.fastRelationTests = fastRelationTests;
	}

	private Path pathTmpTreeNodes;
	private Path pathTmpTreeWays;
	private Path pathTmpTreeSimpleRelations;
	private Path pathTmpTreeComplexRelations;
	private Path pathTmpTreeAdditionalNodes;
	private Path pathTmpTreeAdditionalWays;
	private Path pathTmpSimpleNodes;
	private Path pathTmpSimpleWays;
	private Path pathTmpSimpleRelations;
	private Path pathTmpComplexNodes;
	private Path pathTmpComplexWays;
	private Path pathTmpComplexRelations;

	private GeometryFactory factory = new GeometryFactory();

	private DataTree tree;
	private DataTreeFiles filesTreeNodes;
	private DataTreeFiles filesTreeWays;
	private DataTreeFiles filesTreeSimpleRelations;
	private DataTreeFiles filesTreeComplexRelations;

	// Lists of files that need to be merged in the end
	private List<OsmFileInput> filesNodes = new ArrayList<>();
	private List<OsmFileInput> filesWays = new ArrayList<>();
	private List<OsmFileInput> filesSimpleRelations = new ArrayList<>();
	private List<OsmFileInput> filesComplexRelations = new ArrayList<>();

	private int nNodes = 0;
	private int nWays = 0;
	private int nSimpleRelations = 0;
	private int nComplexRelations = 0;

	private int tmpIndexTree = 0;
	private int tmpIndexSimple = 0;
	private int tmpIndexComplex = 0;

	public void execute() throws IOException
	{
		createTemporaryDirectory();

		// Query setup

		openTree();

		Geometry box = factory.toGeometry(queryEnvelope);
		List<Node> leafs = tree.query(box);

		// Query data tree

		for (Node leaf : leafs) {
			String leafName = Long.toHexString(leaf.getPath());

			if (test.contains(leaf.getEnvelope())) {
				System.out.println("Leaf is completely contained: " + leafName);
				addCompletelyContainedLeaf(leaf);
				continue;
			}

			System.out.println("Loading data from leaf: " + leafName);
			addIntersectingLeaf(leaf);
		}

		System.out.println(String.format("Total number of nodes: %d", nNodes));
		System.out.println(String.format("Total number of ways: %d", nWays));
		System.out.println(String.format(
				"Total number of simple relations: %d", nSimpleRelations));
		System.out.println(String.format(
				"Total number of complex relations: %d", nComplexRelations));

		// Query relations

		List<IdBboxEntry> entriesSimple = IdBboxUtil
				.read(pathSimpleRelationsBboxes);
		List<IdBboxEntry> entriesComplex = IdBboxUtil
				.read(pathComplexRelationsBboxes);

		for (IdBboxEntry entry : entriesSimple) {
			long id = entry.getId();
			if (test.contains(entry.getEnvelope())) {
				System.out.println("Simple batch completely contained: " + id);
				addCompletelyContainedBatch(pathSimpleRelations, id,
						filesSimpleRelations);
			} else if (test.intersects(entry.getEnvelope())) {
				System.out.println("Loading data from simple batch: " + id);
				tmpIndexSimple++;
				String tmpFilenames = filename(tmpIndexSimple);

				Path pathDir = pathSimpleRelations.resolve(Long.toString(entry
						.getId()));
				Path pathNodes = pathDir.resolve(fileNamesRelationNodes);
				Path pathWays = pathDir.resolve(fileNamesRelationWays);
				Path pathRelations = pathDir
						.resolve(fileNamesRelationRelations);

				Path pathOutNodes = pathTmpSimpleNodes.resolve(tmpFilenames);
				Path pathOutWays = pathTmpSimpleWays.resolve(tmpFilenames);
				Path pathOutRelations = pathTmpSimpleRelations
						.resolve(tmpFilenames);

				runRelationsQuery(true, tmpFilenames, pathNodes, pathWays,
						pathRelations, pathOutNodes, pathOutWays,
						pathOutRelations);
			}
		}

		for (IdBboxEntry entry : entriesComplex) {
			long id = entry.getId();
			if (test.contains(entry.getEnvelope())) {
				System.out.println("Complex batch completely contained: " + id);
				addCompletelyContainedBatch(pathComplexRelations, id,
						filesComplexRelations);
			} else if (test.intersects(entry.getEnvelope())) {
				System.out.println("Loading data from complex batch: " + id);
				tmpIndexComplex++;
				String tmpFilenames = filename(tmpIndexComplex);

				Path pathDir = pathComplexRelations.resolve(Long.toString(entry
						.getId()));
				Path pathNodes = pathDir.resolve(fileNamesRelationNodes);
				Path pathWays = pathDir.resolve(fileNamesRelationWays);
				Path pathRelations = pathDir
						.resolve(fileNamesRelationRelations);

				Path pathOutNodes = pathTmpComplexNodes.resolve(tmpFilenames);
				Path pathOutWays = pathTmpComplexWays.resolve(tmpFilenames);
				Path pathOutRelations = pathTmpComplexRelations
						.resolve(tmpFilenames);

				runRelationsQuery(false, tmpFilenames, pathNodes, pathWays,
						pathRelations, pathOutNodes, pathOutWays,
						pathOutRelations);
			}
		}

		// Merge intermediate files

		OsmStreamOutput output = createOutput(pathOutput);

		List<OsmFileInput> mergeFiles = new ArrayList<>();

		mergeFiles.addAll(filesNodes);
		mergeFiles.addAll(filesWays);
		mergeFiles.addAll(filesSimpleRelations);
		mergeFiles.addAll(filesComplexRelations);

		System.out
				.println(String.format("Merging %d files", mergeFiles.size()));

		List<OsmIteratorInput> mergeIteratorInputs = new ArrayList<>();
		List<OsmIterator> mergeIterators = new ArrayList<>();
		for (OsmFileInput input : mergeFiles) {
			OsmIteratorInput iteratorInput = input.createIterator(true,
					writeMetadata);
			mergeIteratorInputs.add(iteratorInput);
			mergeIterators.add(iteratorInput.getIterator());
		}

		SortedMerge merge = new SortedMerge(output.getOsmOutput(),
				mergeIterators);
		merge.run();

		for (OsmIteratorInput input : mergeIteratorInputs) {
			input.close();
		}

		output.close();

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
		System.out.println("Temporary directory: " + pathTmp);
		Files.createDirectories(pathTmp);
		if (!Files.isDirectory(pathTmp)) {
			System.out
					.println("Unable to create temporary directory for intermediate files");
			System.exit(1);
		}
		if (pathTmp.toFile().listFiles().length != 0) {
			System.out
					.println("Temporary directory for intermediate files is not empty");
			System.exit(1);
		}
		System.out.println("Storing intermediate files here: " + pathTmp);

		// Create sub-directories for intermediate files

		Path pathTmpTree = pathTmp.resolve("tree");
		Path pathTmpSimple = pathTmp.resolve("simple-relations");
		Path pathTmpComplex = pathTmp.resolve("complex-relations");

		pathTmpTreeNodes = pathTmpTree.resolve("nodes");
		pathTmpTreeWays = pathTmpTree.resolve("ways");
		pathTmpTreeSimpleRelations = pathTmpTree.resolve("relations.simple");
		pathTmpTreeComplexRelations = pathTmpTree.resolve("relations.complex");
		pathTmpTreeAdditionalNodes = pathTmpTree.resolve("nodes-extra");
		pathTmpTreeAdditionalWays = pathTmpTree.resolve("ways-extra");

		pathTmpSimpleNodes = pathTmpSimple.resolve("nodes");
		pathTmpSimpleWays = pathTmpSimple.resolve("ways");
		pathTmpSimpleRelations = pathTmpSimple.resolve("relations");
		pathTmpComplexNodes = pathTmpComplex.resolve("nodes");
		pathTmpComplexWays = pathTmpComplex.resolve("ways");
		pathTmpComplexRelations = pathTmpComplex.resolve("relations");

		Files.createDirectory(pathTmpTree);
		Files.createDirectory(pathTmpSimple);
		Files.createDirectory(pathTmpComplex);

		Files.createDirectory(pathTmpTreeNodes);
		Files.createDirectory(pathTmpTreeWays);
		Files.createDirectory(pathTmpTreeSimpleRelations);
		Files.createDirectory(pathTmpTreeComplexRelations);
		Files.createDirectory(pathTmpTreeAdditionalNodes);
		Files.createDirectory(pathTmpTreeAdditionalWays);

		Files.createDirectory(pathTmpSimpleNodes);
		Files.createDirectory(pathTmpSimpleWays);
		Files.createDirectory(pathTmpSimpleRelations);
		Files.createDirectory(pathTmpComplexNodes);
		Files.createDirectory(pathTmpComplexWays);
		Files.createDirectory(pathTmpComplexRelations);
	}

	private void openTree() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());

		filesTreeNodes = new DataTreeFiles(pathTree, fileNamesTreeNodes);
		filesTreeWays = new DataTreeFiles(pathTree, fileNamesTreeWays);
		filesTreeSimpleRelations = new DataTreeFiles(pathTree,
				fileNamesTreeSimpleRelations);
		filesTreeComplexRelations = new DataTreeFiles(pathTree,
				fileNamesTreeComplexRelations);
	}

	private OsmFileInput input(Path path)
	{
		return new OsmFileInput(path, inputFormat);
	}

	private OsmFileInput intermediate(Path path)
	{
		return new OsmFileInput(path, outputFormat);
	}

	private void addCompletelyContainedLeaf(Node leaf)
	{
		filesNodes.add(input(filesTreeNodes.getPath(leaf)));
		filesWays.add(input(filesTreeWays.getPath(leaf)));
		filesSimpleRelations.add(input(filesTreeSimpleRelations.getPath(leaf)));
		filesComplexRelations
				.add(input(filesTreeComplexRelations.getPath(leaf)));
	}

	private void addIntersectingLeaf(Node leaf) throws IOException
	{
		LeafQuery leafQuery = new LeafQuery(test, filesTreeNodes,
				filesTreeWays, filesTreeSimpleRelations,
				filesTreeComplexRelations, inputFormat, outputFormat,
				writeMetadata, pbfConfig, tboConfig, fastRelationTests);

		tmpIndexTree++;

		String tmpFilenames = filename(tmpIndexTree);
		Path pathOutNodes = pathTmpTreeNodes.resolve(tmpFilenames);
		Path pathOutWays = pathTmpTreeWays.resolve(tmpFilenames);
		Path pathOutSimpleRelations = pathTmpTreeSimpleRelations
				.resolve(tmpFilenames);
		Path pathOutComplexRelations = pathTmpTreeComplexRelations
				.resolve(tmpFilenames);
		Path pathOutAdditionalNodes = pathTmpTreeAdditionalNodes
				.resolve(tmpFilenames);
		Path pathOutAdditionalWays = pathTmpTreeAdditionalWays
				.resolve(tmpFilenames);

		QueryResult results = leafQuery.execute(leaf, pathOutNodes,
				pathOutWays, pathOutSimpleRelations, pathOutComplexRelations,
				pathOutAdditionalNodes, pathOutAdditionalWays);

		nNodes += results.getNumNodes();
		nWays += results.getNumWays();
		nSimpleRelations += results.getNumSimpleRelations();
		nComplexRelations += results.getNumComplexRelations();

		filesNodes.add(intermediate(pathOutNodes));
		filesNodes.add(intermediate(pathOutAdditionalNodes));
		filesWays.add(intermediate(pathOutWays));
		filesWays.add(intermediate(pathOutAdditionalWays));
		filesSimpleRelations.add(intermediate(pathOutSimpleRelations));
		filesComplexRelations.add(intermediate(pathOutComplexRelations));

		System.out.println(String.format("Found %d nodes",
				results.getNumNodes()));
		System.out
				.println(String.format("Found %d ways", results.getNumWays()));
		System.out.println(String.format("Found %d simple relations",
				results.getNumSimpleRelations()));
		System.out.println(String.format("Found %d complex relations",
				results.getNumComplexRelations()));
	}

	private void addCompletelyContainedBatch(Path pathRelations, long id,
			List<OsmFileInput> filesRelations)
	{
		Path path = pathRelations.resolve(Long.toString(id));
		filesNodes.add(input(path.resolve(fileNamesRelationNodes)));
		filesWays.add(input(path.resolve(fileNamesRelationWays)));
		filesRelations.add(input(path.resolve(fileNamesRelationRelations)));
	}

	private void runRelationsQuery(boolean simple, String tmpFilenames,
			Path pathNodes, Path pathWays, Path pathRelations,
			Path pathOutNodes, Path pathOutWays, Path pathOutRelations)
			throws IOException
	{
		InMemoryListDataSet dataNodes = read(pathNodes);
		InMemoryListDataSet dataWays = read(pathWays);
		InMemoryListDataSet dataRelations = read(pathRelations);

		OsmStreamOutput outRelations = createOutput(pathOutRelations);
		RelationQueryBag queryBag = new RelationQueryBag(outRelations);

		if (simple) {
			SimpleRelationsQuery simpleRelationsQuery = new SimpleRelationsQuery(
					dataNodes, dataWays, dataRelations, test, fastRelationTests);
			simpleRelationsQuery.execute(queryBag);
		} else {
			ComplexRelationsQuery complexRelationsQuery = new ComplexRelationsQuery(
					dataNodes, dataWays, dataRelations, test, fastRelationTests);
			complexRelationsQuery.execute(queryBag);
		}

		finish(outRelations);

		OsmStreamOutput outputNodes = createOutput(pathOutNodes);
		QueryUtil.writeNodes(queryBag.additionalNodes,
				outputNodes.getOsmOutput());
		finish(outputNodes);

		OsmStreamOutput outputWays = createOutput(pathOutWays);
		QueryUtil.writeWays(queryBag.additionalWays, outputWays.getOsmOutput());
		finish(outputWays);

		filesNodes.add(intermediate(pathOutNodes));
		filesWays.add(intermediate(pathOutWays));
		filesSimpleRelations.add(intermediate(pathOutRelations));
	}

}

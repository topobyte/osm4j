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
import java.io.OutputStream;
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
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.merge.sorted.SortedMerge;

public class Query
{

	private Path pathOutput;
	private Path pathTmp;
	private Path pathTree;
	private Path pathSimpleRelations;
	private Path pathComplexRelations;

	private String fileNamesTreeNodes;
	private String fileNamesTreeWays;
	private String fileNamesTreeSimpleRelations;
	private String fileNamesTreeComplexRelations;
	private String fileNamesRelationNodes;
	private String fileNamesRelationWays;
	private String fileNamesRelationRelations;

	private Envelope queryEnvelope;
	private PredicateEvaluator test;

	private FileFormat inputFormat;
	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	private boolean keepTmp;

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
			PbfConfig pbfConfig, TboConfig tboConfig, boolean keepTmp)
	{
		this.pathOutput = pathOutput;
		this.pathTmp = pathTmp;
		this.pathTree = pathTree;
		this.pathSimpleRelations = pathSimpleRelations;
		this.pathComplexRelations = pathComplexRelations;
		this.fileNamesTreeNodes = fileNamesTreeNodes;
		this.fileNamesTreeWays = fileNamesTreeWays;
		this.fileNamesTreeSimpleRelations = fileNamesTreeSimpleRelations;
		this.fileNamesTreeComplexRelations = fileNamesTreeComplexRelations;
		this.fileNamesRelationNodes = fileNamesRelationNodes;
		this.fileNamesRelationWays = fileNamesRelationWays;
		this.fileNamesRelationRelations = fileNamesRelationRelations;
		this.queryEnvelope = queryEnvelope;
		this.test = test;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
		this.keepTmp = keepTmp;
	}

	private Path pathTmpNodes;
	private Path pathTmpWays;
	private Path pathTmpSimpleRelations;
	private Path pathTmpComplexRelations;
	private Path pathTmpAdditionalNodes;
	private Path pathTmpAdditionalWays;

	private GeometryFactory factory = new GeometryFactory();

	private DataTree tree;
	private DataTreeFiles filesTreeNodes;
	private DataTreeFiles filesTreeWays;
	private DataTreeFiles filesTreeSimpleRelations;
	private DataTreeFiles filesTreeComplexRelations;

	// Lists of files that need to be merged in the end
	private List<OsmFileInput> pathsNodes = new ArrayList<>();
	private List<OsmFileInput> pathsWays = new ArrayList<>();
	private List<OsmFileInput> pathsSimpleRelations = new ArrayList<>();
	private List<OsmFileInput> pathsComplexRelations = new ArrayList<>();

	private int nNodes = 0;
	private int nWays = 0;
	private int nSimpleRelations = 0;
	private int nComplexRelations = 0;

	private int tmpIndex = 0;

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

		// TODO: implement this

		// Merge intermediate files

		OutputStream output = StreamUtil.bufferedOutputStream(pathOutput);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputFormat, writeMetadata, pbfConfig, tboConfig);

		List<OsmFileInput> mergeFiles = new ArrayList<>();

		mergeFiles.addAll(pathsNodes);
		mergeFiles.addAll(pathsWays);
		mergeFiles.addAll(pathsSimpleRelations);
		mergeFiles.addAll(pathsComplexRelations);

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

		SortedMerge merge = new SortedMerge(osmOutput, mergeIterators);
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

		pathTmpNodes = pathTmp.resolve("nodes");
		pathTmpWays = pathTmp.resolve("ways");
		pathTmpSimpleRelations = pathTmp.resolve("relations.simple");
		pathTmpComplexRelations = pathTmp.resolve("relations.complex");
		pathTmpAdditionalNodes = pathTmp.resolve("nodes-extra");
		pathTmpAdditionalWays = pathTmp.resolve("ways-extra");

		Files.createDirectory(pathTmpNodes);
		Files.createDirectory(pathTmpWays);
		Files.createDirectory(pathTmpSimpleRelations);
		Files.createDirectory(pathTmpComplexRelations);
		Files.createDirectory(pathTmpAdditionalNodes);
		Files.createDirectory(pathTmpAdditionalWays);
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
		input(filesTreeNodes.getPath(leaf));
		pathsNodes.add(input(filesTreeNodes.getPath(leaf)));
		pathsWays.add(input(filesTreeWays.getPath(leaf)));
		pathsSimpleRelations.add(input(filesTreeSimpleRelations.getPath(leaf)));
		pathsComplexRelations
				.add(input(filesTreeComplexRelations.getPath(leaf)));
	}

	private void addIntersectingLeaf(Node leaf) throws IOException
	{
		LeafQuery leafQuery = new LeafQuery(test, filesTreeNodes,
				filesTreeWays, filesTreeSimpleRelations,
				filesTreeComplexRelations, inputFormat, outputFormat,
				writeMetadata, pbfConfig, tboConfig);

		tmpIndex++;

		String tmpFilenames = String.format("%d%s", tmpIndex,
				OsmIoUtils.extension(outputFormat));
		Path pathOutNodes = pathTmpNodes.resolve(tmpFilenames);
		Path pathOutWays = pathTmpWays.resolve(tmpFilenames);
		Path pathOutSimpleRelations = pathTmpSimpleRelations
				.resolve(tmpFilenames);
		Path pathOutComplexRelations = pathTmpComplexRelations
				.resolve(tmpFilenames);
		Path pathOutAdditionalNodes = pathTmpAdditionalNodes
				.resolve(tmpFilenames);
		Path pathOutAdditionalWays = pathTmpAdditionalWays
				.resolve(tmpFilenames);

		QueryResult results = leafQuery.execute(leaf, pathOutNodes,
				pathOutWays, pathOutSimpleRelations, pathOutComplexRelations,
				pathOutAdditionalNodes, pathOutAdditionalWays);

		nNodes += results.getNumNodes();
		nWays += results.getNumWays();
		nSimpleRelations += results.getNumSimpleRelations();
		nComplexRelations += results.getNumComplexRelations();

		pathsNodes.add(intermediate(pathOutNodes));
		pathsNodes.add(intermediate(pathOutAdditionalNodes));
		pathsWays.add(intermediate(pathOutWays));
		pathsWays.add(intermediate(pathOutAdditionalWays));
		pathsSimpleRelations.add(intermediate(pathOutSimpleRelations));
		pathsComplexRelations.add(intermediate(pathOutComplexRelations));

		System.out.println(String.format("Found %d nodes",
				results.getNumNodes()));
		System.out
				.println(String.format("Found %d ways", results.getNumWays()));
		System.out.println(String.format("Found %d simple relations",
				results.getNumSimpleRelations()));
		System.out.println(String.format("Found %d complex relations",
				results.getNumComplexRelations()));
	}

}

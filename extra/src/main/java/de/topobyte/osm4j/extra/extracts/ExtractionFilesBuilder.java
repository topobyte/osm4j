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

package de.topobyte.osm4j.extra.extracts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.extra.batch.BatchFilesUtil;
import de.topobyte.osm4j.extra.datatree.TreeFilesMerger;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreatorMaxNodes;
import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesExtractor;
import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesFinder;
import de.topobyte.osm4j.extra.datatree.ways.WaysDistributor;
import de.topobyte.osm4j.extra.datatree.ways.WaysToTreeMapper;
import de.topobyte.osm4j.extra.relations.ComplexRelationsDistributor;
import de.topobyte.osm4j.extra.relations.NonTreeRelationsSplitter;
import de.topobyte.osm4j.extra.relations.RelationsMemberCollector;
import de.topobyte.osm4j.extra.relations.RelationsSeparator;
import de.topobyte.osm4j.extra.relations.RelationsSplitterAndMemberCollector;
import de.topobyte.osm4j.extra.relations.SimpleRelationsDistributor;
import de.topobyte.osm4j.extra.ways.WaysSorterByFirstNodeId;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.split.EntitySplitter;

public class ExtractionFilesBuilder
{

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	private Path pathInput;
	private FileFormat inputFormat;
	private Path pathOutput;
	private OsmOutputConfig outputConfig;
	private int maxNodes;

	private Path pathTree;
	private Path pathWaysByNodes;

	private Path pathNodes;
	private Path pathWays;
	private Path pathRelations;

	private Path pathSimpleRelations;
	private Path pathComplexRelations;
	private Path pathSimpleRelationsDir;
	private Path pathComplexRelationsDir;
	private Path pathSimpleRelationsNonTree;
	private Path pathComplexRelationsNonTree;
	private Path pathSimpleRelationsNonTreeBboxes;
	private Path pathComplexRelationsNonTreeBboxes;
	private Path pathSimpleRelationsEmpty;
	private Path pathComplexRelationsEmpty;
	private Path pathSimpleRelationsSorted;
	private Path pathComplexRelationsSorted;

	public ExtractionFilesBuilder(Path pathInput, FileFormat inputFormat,
			Path pathOutput, OsmOutputConfig outputConfig, int maxNodes)
	{
		this.pathInput = pathInput;
		this.inputFormat = inputFormat;
		this.pathOutput = pathOutput;
		this.outputConfig = outputConfig;
		this.maxNodes = maxNodes;
	}

	public void execute() throws IOException, OsmInputException
	{
		System.out.println("Output directory: " + pathOutput);
		Files.createDirectories(pathOutput);
		if (!Files.isDirectory(pathOutput)) {
			System.out.println("Unable to create output directory");
			System.exit(1);
		}
		if (pathOutput.toFile().listFiles().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}

		String extension = OsmIoUtils.extension(outputConfig.getFileFormat());

		pathNodes = pathOutput.resolve("nodes" + extension);
		pathWays = pathOutput.resolve("ways" + extension);
		pathRelations = pathOutput.resolve("relations" + extension);

		pathTree = pathOutput.resolve("tree");
		pathWaysByNodes = pathOutput.resolve("waysbynodes");

		pathSimpleRelations = pathOutput
				.resolve("relations.simple" + extension);
		pathComplexRelations = pathOutput.resolve("relations.complex"
				+ extension);

		pathSimpleRelationsDir = pathOutput.resolve("relations.simple");
		pathComplexRelationsDir = pathOutput.resolve("relations.complex");
		pathSimpleRelationsNonTree = pathOutput
				.resolve("relations.simple.nontree" + extension);
		pathComplexRelationsNonTree = pathOutput
				.resolve("relations.complex.nontree" + extension);
		pathSimpleRelationsNonTreeBboxes = pathOutput
				.resolve("relations.simple.nontree.bboxlist");
		pathComplexRelationsNonTreeBboxes = pathOutput
				.resolve("relations.complex.nontree.bboxlist");
		pathSimpleRelationsEmpty = pathOutput.resolve("relations.simple.empty"
				+ extension);
		pathComplexRelationsEmpty = pathOutput
				.resolve("relations.complex.empty" + extension);
		pathSimpleRelationsSorted = pathOutput
				.resolve("relations.simple.sorted");
		pathComplexRelationsSorted = pathOutput
				.resolve("relations.complex.sorted");

		OsmFileInput fileInput = new OsmFileInput(pathInput, inputFormat);

		OsmFileInput fileInputNodes = new OsmFileInput(pathNodes,
				outputConfig.getFileFormat());
		OsmFileInput fileInputWays = new OsmFileInput(pathWays,
				outputConfig.getFileFormat());
		OsmFileInput fileInputRelations = new OsmFileInput(pathRelations,
				outputConfig.getFileFormat());

		String fileNamesFinalNodes = "allnodes" + extension;
		String fileNamesFinalWays = "allways" + extension;
		String fileNamesFinalRelationsSimple = "relations.simple" + extension;
		String fileNamesFinalRelationsComplex = "relations.complex" + extension;

		String fileNamesInitialNodes = "nodes" + extension;
		String fileNamesInitialWays = "dways" + extension;
		String fileNamesMissingWayNodeIds = "dways-missing.ids";
		String fileNamesMissingNodes = "missing-nodes" + extension;
		String fileNamesDistributedWays = "ways-unsorted" + extension;
		String fileNamesDistributedNodes = "nodes-unsorted" + extension;

		String fileNamesRelations = "relations" + extension;

		// Split entities

		OsmIteratorInput input = fileInput.createIterator(true,
				outputConfig.isWriteMetadata());

		EntitySplitter splitter = new EntitySplitter(input.getIterator(),
				pathNodes, pathWays, pathRelations, outputConfig);
		splitter.execute();

		input.close();

		// Create node tree

		NodeTreeCreatorMaxNodes creator = new NodeTreeCreatorMaxNodes(
				fileInputNodes, maxNodes, SPLIT_INITIAL, SPLIT_ITERATION,
				pathTree, fileNamesInitialNodes, outputConfig);

		creator.init();
		creator.buildTree();

		// Sort ways by first node id

		OsmIteratorInput inputWays = fileInputWays.createIterator(true,
				outputConfig.isWriteMetadata());

		WaysSorterByFirstNodeId waysSorter = new WaysSorterByFirstNodeId(
				inputWays.getIterator(), pathWaysByNodes, outputConfig);
		waysSorter.execute();

		inputWays.close();

		// Map ways to tree

		OsmIteratorInput inputNodes = fileInputNodes.createIterator(true,
				outputConfig.isWriteMetadata());

		WaysToTreeMapper waysMapper = new WaysToTreeMapper(
				inputNodes.getIterator(), pathTree, pathWaysByNodes,
				outputConfig.getFileFormat(), fileNamesInitialWays,
				outputConfig);
		waysMapper.prepare();
		waysMapper.execute();

		inputNodes.close();

		// Find missing way nodes

		MissingWayNodesFinder wayNodesFinder = new MissingWayNodesFinder(
				pathTree, pathTree, pathTree, fileNamesInitialNodes,
				fileNamesInitialWays, fileNamesMissingWayNodeIds,
				outputConfig.getFileFormat(), outputConfig.getFileFormat());
		wayNodesFinder.execute();

		// Extract missing way nodes

		inputNodes = fileInputNodes.createIterator(true,
				outputConfig.isWriteMetadata());

		MissingWayNodesExtractor wayNodesExtractor = new MissingWayNodesExtractor(
				inputNodes.getIterator(), pathTree, fileNamesMissingWayNodeIds,
				pathTree, fileNamesMissingNodes, outputConfig);
		wayNodesExtractor.execute();

		inputNodes.close();

		for (Path path : BatchFilesUtil.getPaths(pathTree,
				fileNamesMissingWayNodeIds)) {
			Files.delete(path);
		}

		// Distribute ways

		WaysDistributor waysDistributor = new WaysDistributor(pathTree,
				fileNamesInitialNodes, fileNamesMissingNodes,
				fileNamesInitialWays, fileNamesDistributedWays,
				fileNamesDistributedNodes, outputConfig.getFileFormat(),
				outputConfig.getFileFormat(), outputConfig);
		waysDistributor.execute();

		// Merge nodes

		List<String> fileNamesSortedNodes = new ArrayList<>();
		List<String> fileNamesUnsortedNodes = new ArrayList<>();
		fileNamesSortedNodes.add(fileNamesInitialNodes);
		fileNamesSortedNodes.add(fileNamesMissingNodes);
		fileNamesUnsortedNodes.add(fileNamesDistributedNodes);
		TreeFilesMerger nodesMerger = new TreeFilesMerger(pathTree,
				fileNamesSortedNodes, fileNamesUnsortedNodes,
				fileNamesFinalNodes, outputConfig.getFileFormat(),
				outputConfig, true);
		nodesMerger.execute();

		// Merge ways

		List<String> fileNamesSortedWays = new ArrayList<>();
		List<String> fileNamesUnsortedWays = new ArrayList<>();
		fileNamesSortedWays.add(fileNamesInitialWays);
		fileNamesUnsortedWays.add(fileNamesDistributedWays);
		TreeFilesMerger waysMerger = new TreeFilesMerger(pathTree,
				fileNamesSortedWays, fileNamesUnsortedWays, fileNamesFinalWays,
				outputConfig.getFileFormat(), outputConfig, true);
		waysMerger.execute();

		// Separate relations

		RelationsSeparator separator = new RelationsSeparator(
				fileInputRelations, pathSimpleRelations, pathComplexRelations,
				outputConfig);
		separator.execute();

		// Split relations and collect members

		OsmFileInput inputSimpleRelations = new OsmFileInput(
				pathSimpleRelations, outputConfig.getFileFormat());
		OsmFileInput inputComplexRelations = new OsmFileInput(
				pathComplexRelations, outputConfig.getFileFormat());

		RelationsSplitterAndMemberCollector relationSplitter = new RelationsSplitterAndMemberCollector(
				inputSimpleRelations, inputComplexRelations,
				pathSimpleRelationsDir, pathComplexRelationsDir,
				fileNamesRelations, fileInputWays, fileInputNodes, outputConfig);
		relationSplitter.execute();

		// Distribute relations

		String fileNamesNodes = RelationsMemberCollector.FILE_NAMES_NODE_BASENAME
				+ extension;
		String fileNamesWays = RelationsMemberCollector.FILE_NAMES_WAY_BASENAME
				+ extension;

		SimpleRelationsDistributor simpleRelationsDistributor = new SimpleRelationsDistributor(
				pathTree, pathSimpleRelationsDir, pathSimpleRelationsEmpty,
				pathSimpleRelationsNonTree, pathSimpleRelationsNonTreeBboxes,
				fileNamesRelations, fileNamesWays, fileNamesNodes,
				fileNamesFinalRelationsSimple, outputConfig.getFileFormat(),
				outputConfig);
		simpleRelationsDistributor.execute();

		ComplexRelationsDistributor complexRelationsDistributor = new ComplexRelationsDistributor(
				pathTree, pathComplexRelationsDir, pathComplexRelationsEmpty,
				pathComplexRelationsNonTree, pathComplexRelationsNonTreeBboxes,
				fileNamesRelations, fileNamesWays, fileNamesNodes,
				fileNamesFinalRelationsComplex, outputConfig.getFileFormat(),
				outputConfig);
		complexRelationsDistributor.execute();

		// Sort non-tree relations

		NonTreeRelationsSplitter nonTreeSplitter = new NonTreeRelationsSplitter(
				pathSimpleRelationsNonTree, pathComplexRelationsNonTree,
				pathSimpleRelationsNonTreeBboxes,
				pathComplexRelationsNonTreeBboxes, pathSimpleRelationsDir,
				pathComplexRelationsDir, pathSimpleRelationsSorted,
				pathComplexRelationsSorted, outputConfig.getFileFormat(),
				outputConfig);
		nonTreeSplitter.execute();
	}

}

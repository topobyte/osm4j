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
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.batch.BatchFilesUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.merge.ThreadedTreeFilesMerger;
import de.topobyte.osm4j.extra.datatree.merge.TreeFilesMerger;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreatorMaxNodes;
import de.topobyte.osm4j.extra.datatree.nodetree.count.NodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.count.ThreadedNodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.ThreadedNodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.output.ClosingDataTreeOutputFactory;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesExtractor;
import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesFinder;
import de.topobyte.osm4j.extra.datatree.ways.ThreadedMissingWayNodesFinder;
import de.topobyte.osm4j.extra.datatree.ways.ThreadedWaysDistributor;
import de.topobyte.osm4j.extra.datatree.ways.ThreadedWaysToTreeMapper;
import de.topobyte.osm4j.extra.datatree.ways.WaysDistributor;
import de.topobyte.osm4j.extra.datatree.ways.WaysToTreeMapper;
import de.topobyte.osm4j.extra.relations.ComplexRelationsDistributor;
import de.topobyte.osm4j.extra.relations.NonTreeRelationsSplitter;
import de.topobyte.osm4j.extra.relations.RelationsMemberCollector;
import de.topobyte.osm4j.extra.relations.RelationsSeparator;
import de.topobyte.osm4j.extra.relations.RelationsSplitterAndMemberCollector;
import de.topobyte.osm4j.extra.relations.SimpleRelationsDistributor;
import de.topobyte.osm4j.extra.ways.ThreadedWaysSorterByFirstNodeId;
import de.topobyte.osm4j.extra.ways.WaysSorterByFirstNodeId;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.config.limit.ElementCountLimit;
import de.topobyte.osm4j.utils.config.limit.RelationMemberLimit;
import de.topobyte.osm4j.utils.config.limit.WayNodeLimit;
import de.topobyte.osm4j.utils.split.ThreadedEntitySplitter;

public class ExtractionFilesBuilder
{

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	private Path pathInput;
	private FileFormat inputFormat;
	private Path pathOutput;
	private int maxNodes;
	private boolean includeMetadata;

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
			Path pathOutput, int maxNodes, boolean includeMetadata)
	{
		this.pathInput = pathInput;
		this.inputFormat = inputFormat;
		this.pathOutput = pathOutput;
		this.maxNodes = maxNodes;
		this.includeMetadata = includeMetadata;
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

		FileFormat outputFormat = FileFormat.TBO;

		String extension = OsmIoUtils.extension(outputFormat);

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

		OsmFileInput fileInputNodes = new OsmFileInput(pathNodes, outputFormat);
		OsmFileInput fileInputWays = new OsmFileInput(pathWays, outputFormat);
		OsmFileInput fileInputRelations = new OsmFileInput(pathRelations,
				outputFormat);

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

		OsmOutputConfig outputConfigSplit = new OsmOutputConfig(outputFormat,
				includeMetadata);
		OsmOutputConfig outputConfigTree = new OsmOutputConfig(outputFormat,
				includeMetadata);
		OsmOutputConfig outputConfigWays = new OsmOutputConfig(outputFormat,
				includeMetadata);
		OsmOutputConfig outputConfigRelations = new OsmOutputConfig(
				outputFormat, includeMetadata);

		outputConfigTree.getTboConfig().setLimitNodes(
				new ElementCountLimit(1024));
		outputConfigTree.getTboConfig().setLimitWays(new WayNodeLimit(2048));
		outputConfigTree.getTboConfig().setLimitRelations(
				new RelationMemberLimit(2048));

		outputConfigRelations.getTboConfig().setLimitRelations(
				new RelationMemberLimit(1024));

		OsmOutputConfig outputConfigTreeFinal = new OsmOutputConfig(
				outputFormat, includeMetadata);

		// Determine bounds

		OsmIteratorInput inputBounds = fileInput.createIterator(false, false);

		if (!inputBounds.getIterator().hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}

		OsmBounds bounds = inputBounds.getIterator().getBounds();
		System.out.println("bounds: " + bounds);

		inputBounds.close();

		// Split entities

		OsmIteratorInput input = fileInput
				.createIterator(true, includeMetadata);

		ThreadedEntitySplitter splitter = new ThreadedEntitySplitter(
				input.getIterator(), pathNodes, pathWays, pathRelations,
				outputConfigSplit, 10000, 200);
		splitter.execute();

		input.close();

		// Create node tree

		DataTree tree = DataTreeUtil.initNewTree(pathTree, bounds);

		DataTreeFiles treeFiles = new DataTreeFiles(pathTree,
				fileNamesInitialNodes);
		DataTreeOutputFactory dataTreeOutputFactory = new ClosingDataTreeOutputFactory(
				treeFiles, outputConfigTree);

		NodeTreeLeafCounterFactory counterFactory = new ThreadedNodeTreeLeafCounterFactory();
		NodeTreeDistributorFactory distributorFactory = new ThreadedNodeTreeDistributorFactory();

		NodeTreeCreatorMaxNodes creator = new NodeTreeCreatorMaxNodes(tree,
				fileInputNodes, dataTreeOutputFactory, maxNodes, SPLIT_INITIAL,
				SPLIT_ITERATION, pathTree, fileNamesInitialNodes,
				outputConfigTree, counterFactory, distributorFactory);

		creator.buildTree();

		// Sort ways by first node id

		OsmIteratorInput inputWays = fileInputWays.createIterator(true,
				includeMetadata);

		WaysSorterByFirstNodeId waysSorter = new ThreadedWaysSorterByFirstNodeId(
				inputWays.getIterator(), pathWaysByNodes, outputConfigWays);
		waysSorter.execute();

		inputWays.close();

		// Map ways to tree

		OsmIteratorInput inputNodes = fileInputNodes.createIterator(true,
				includeMetadata);

		WaysToTreeMapper waysMapper = new ThreadedWaysToTreeMapper(
				inputNodes.getIterator(), pathTree, pathWaysByNodes,
				outputFormat, fileNamesInitialWays, outputConfigTree);
		waysMapper.execute();

		inputNodes.close();

		// Find missing way nodes

		MissingWayNodesFinder wayNodesFinder = new ThreadedMissingWayNodesFinder(
				pathTree, pathTree, pathTree, fileNamesInitialNodes,
				fileNamesInitialWays, fileNamesMissingWayNodeIds, outputFormat,
				outputFormat);
		wayNodesFinder.execute();

		// Extract missing way nodes

		inputNodes = fileInputNodes.createIterator(true, includeMetadata);

		boolean threaded = true;
		MissingWayNodesExtractor wayNodesExtractor = new MissingWayNodesExtractor(
				inputNodes.getIterator(), pathTree, fileNamesMissingWayNodeIds,
				pathTree, fileNamesMissingNodes, outputConfigTree, threaded);
		wayNodesExtractor.execute();

		inputNodes.close();

		for (Path path : BatchFilesUtil.getPaths(pathTree,
				fileNamesMissingWayNodeIds)) {
			Files.delete(path);
		}

		// Distribute ways

		WaysDistributor waysDistributor = new ThreadedWaysDistributor(pathTree,
				fileNamesInitialNodes, fileNamesMissingNodes,
				fileNamesInitialWays, fileNamesDistributedWays,
				fileNamesDistributedNodes, outputFormat, outputFormat,
				outputConfigTree);
		waysDistributor.execute();

		// Merge nodes

		List<String> fileNamesSortedNodes = new ArrayList<>();
		List<String> fileNamesUnsortedNodes = new ArrayList<>();
		fileNamesSortedNodes.add(fileNamesInitialNodes);
		fileNamesSortedNodes.add(fileNamesMissingNodes);
		fileNamesUnsortedNodes.add(fileNamesDistributedNodes);
		TreeFilesMerger nodesMerger = new ThreadedTreeFilesMerger(pathTree,
				fileNamesSortedNodes, fileNamesUnsortedNodes,
				fileNamesFinalNodes, outputFormat, outputConfigTreeFinal, true);
		nodesMerger.execute();

		// Merge ways

		List<String> fileNamesSortedWays = new ArrayList<>();
		List<String> fileNamesUnsortedWays = new ArrayList<>();
		fileNamesSortedWays.add(fileNamesInitialWays);
		fileNamesUnsortedWays.add(fileNamesDistributedWays);
		TreeFilesMerger waysMerger = new ThreadedTreeFilesMerger(pathTree,
				fileNamesSortedWays, fileNamesUnsortedWays, fileNamesFinalWays,
				outputFormat, outputConfigTreeFinal, true);
		waysMerger.execute();

		// Separate relations

		RelationsSeparator separator = new RelationsSeparator(
				fileInputRelations, pathSimpleRelations, pathComplexRelations,
				outputConfigRelations);
		separator.execute();

		// Split relations and collect members

		OsmFileInput inputSimpleRelations = new OsmFileInput(
				pathSimpleRelations, outputFormat);
		OsmFileInput inputComplexRelations = new OsmFileInput(
				pathComplexRelations, outputFormat);

		RelationsSplitterAndMemberCollector relationSplitter = new RelationsSplitterAndMemberCollector(
				inputSimpleRelations, inputComplexRelations,
				pathSimpleRelationsDir, pathComplexRelationsDir,
				fileNamesRelations, fileInputWays, fileInputNodes,
				outputConfigRelations);
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
				fileNamesFinalRelationsSimple, outputFormat, outputConfigTree);
		simpleRelationsDistributor.execute();

		ComplexRelationsDistributor complexRelationsDistributor = new ComplexRelationsDistributor(
				pathTree, pathComplexRelationsDir, pathComplexRelationsEmpty,
				pathComplexRelationsNonTree, pathComplexRelationsNonTreeBboxes,
				fileNamesRelations, fileNamesWays, fileNamesNodes,
				fileNamesFinalRelationsComplex, outputFormat, outputConfigTree);
		complexRelationsDistributor.execute();

		// Sort non-tree relations

		NonTreeRelationsSplitter nonTreeSplitter = new NonTreeRelationsSplitter(
				pathSimpleRelationsNonTree, pathComplexRelationsNonTree,
				pathSimpleRelationsNonTreeBboxes,
				pathComplexRelationsNonTreeBboxes, pathSimpleRelationsDir,
				pathComplexRelationsDir, pathSimpleRelationsSorted,
				pathComplexRelationsSorted, outputFormat, outputConfigRelations);
		nonTreeSplitter.execute();
	}

}

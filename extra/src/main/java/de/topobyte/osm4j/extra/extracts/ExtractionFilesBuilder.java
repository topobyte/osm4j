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

import org.apache.commons.io.FileUtils;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;
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
import de.topobyte.osm4j.utils.OsmUtils;
import de.topobyte.osm4j.utils.config.limit.ElementCountLimit;
import de.topobyte.osm4j.utils.config.limit.RelationMemberLimit;
import de.topobyte.osm4j.utils.config.limit.WayNodeLimit;
import de.topobyte.osm4j.utils.split.ThreadedEntitySplitter;

public class ExtractionFilesBuilder
{

	private static final String KEY_TOTAL = "total";
	private static final String KEY_SPLIT = "split";
	private static final String KEY_COMPUTE_BBOX = "compute bbox";
	private static final String KEY_NODE_TREE = "build nodetree";
	private static final String KEY_SORT_WAYS = "sort ways by first node id";
	private static final String KEY_MAP_WAYS = "map ways to tree";
	private static final String KEY_FIND_MISSING_WAY_NODES = "find missing way nodes";
	private static final String KEY_EXTRACT_MISSING_WAY_NODES = "extract missing way nodes";
	private static final String KEY_DISTRIBUTE_WAYS = "distribute ways";
	private static final String KEY_MERGE_NODES = "merge tree node files";
	private static final String KEY_MERGE_WAYS = "merge tree way files";
	private static final String KEY_SEPARATE_RELATIONS = "separate simple/complex relations";
	private static final String KEY_SPLIT_RELATIONS = "split relations, collect members";
	private static final String KEY_DISTRIBUTE_RELATIONS = "distribute relations";
	private static final String KEY_SORT_RELATIONS = "sort non-tree relations";
	private static final String KEY_CLEAN_UP = "clean up";

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	private Path pathInput;
	private FileFormat inputFormat;
	private Path pathOutput;
	private int maxNodes;
	private boolean includeMetadata;
	private int maxMembersSimple;
	private int maxMembersComplex;
	private boolean computeBbox;

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
	private Path pathSimpleRelationsSortedBboxes;
	private Path pathComplexRelationsSortedBboxes;

	private boolean keepSplittedNodes = false;
	private boolean keepSplittedWays = false;
	private boolean keepSplittedRelations = false;
	private boolean keepWaysByNodes = false;
	private boolean keepRelations = false;
	private boolean keepRelationBatches = false;
	private boolean keepNonTreeRelations = false;

	private TimeTable t = new TimeTable();

	public ExtractionFilesBuilder(Path pathInput, FileFormat inputFormat,
			Path pathOutput, int maxNodes, boolean includeMetadata,
			int maxMembersSimple, int maxMembersComplex, boolean computeBbox)
	{
		this.pathInput = pathInput;
		this.inputFormat = inputFormat;
		this.pathOutput = pathOutput;
		this.maxNodes = maxNodes;
		this.includeMetadata = includeMetadata;
		this.maxMembersSimple = maxMembersSimple;
		this.maxMembersComplex = maxMembersComplex;
		this.computeBbox = computeBbox;
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
		ExtractionFileNames fileNames = FileNameDefaults
				.forFormat(outputFormat);
		String extension = OsmIoUtils.extension(outputFormat);

		pathNodes = pathOutput.resolve(fileNames.SPLIT_NODES);
		pathWays = pathOutput.resolve(fileNames.SPLIT_WAYS);
		pathRelations = pathOutput.resolve(fileNames.SPLIT_RELATIONS);

		pathTree = pathOutput.resolve(fileNames.TREE);
		pathWaysByNodes = pathOutput.resolve(fileNames.WAYS_BY_NODES);

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
		pathSimpleRelationsEmpty = pathOutput
				.resolve(fileNames.SIMPLE_RELATIONS_EMPTY);
		pathComplexRelationsEmpty = pathOutput
				.resolve(fileNames.COMPLEX_RELATIONS_EMPTY);
		pathSimpleRelationsSorted = pathOutput
				.resolve(fileNames.SIMPLE_RELATIONS);
		pathComplexRelationsSorted = pathOutput
				.resolve(fileNames.COMPLEX_RELATIONS);
		pathSimpleRelationsSortedBboxes = pathOutput
				.resolve(fileNames.SIMPLE_RELATIONS_BBOXES);
		pathComplexRelationsSortedBboxes = pathOutput
				.resolve(fileNames.COMPLEX_RELATIONS_BBOXES);

		OsmFileInput fileInput = new OsmFileInput(pathInput, inputFormat);

		OsmFileInput fileInputNodes = new OsmFileInput(pathNodes, outputFormat);
		OsmFileInput fileInputWays = new OsmFileInput(pathWays, outputFormat);
		OsmFileInput fileInputRelations = new OsmFileInput(pathRelations,
				outputFormat);

		String fileNamesFinalNodes = fileNames.TREE_NODES;
		String fileNamesFinalWays = fileNames.TREE_WAYS;
		String fileNamesFinalRelationsSimple = fileNames.TREE_SIMPLE_RELATIONS;
		String fileNamesFinalRelationsComplex = fileNames.TREE_COMPLEX_RELATIONS;

		String fileNamesInitialNodes = "initial-nodes" + extension;
		String fileNamesInitialWays = "dist-ways" + extension;
		String fileNamesMissingWayNodeIds = "dist-ways-missing.ids";
		String fileNamesMissingNodes = "missing-nodes" + extension;
		String fileNamesDistributedWays = "ways-unsorted" + extension;
		String fileNamesDistributedNodes = "nodes-unsorted" + extension;

		String fileNamesRelations = fileNames.RELATION_RELATIONS;

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
		BBox bbox = null;

		OsmIteratorInput inputBounds = fileInput.createIterator(false, false);

		if (!inputBounds.getIterator().hasBounds() && !computeBbox) {
			System.out.println("Input does not provide bounds"
					+ " and no flag has been set to compute the bounding box");
			System.exit(1);
		}

		if (inputBounds.getIterator().hasBounds()) {
			OsmBounds bounds = inputBounds.getIterator().getBounds();
			bbox = new BBox(bounds.getLeft(), bounds.getBottom(),
					bounds.getRight(), bounds.getTop());

			System.out.println("bounds from file: " + BBoxString.create(bbox));
		}

		inputBounds.close();

		// Split entities
		t.start(KEY_TOTAL);
		t.start(KEY_SPLIT);

		OsmIteratorInput input = fileInput
				.createIterator(true, includeMetadata);

		ThreadedEntitySplitter splitter = new ThreadedEntitySplitter(
				input.getIterator(), pathNodes, pathWays, pathRelations,
				outputConfigSplit, 10000, 200);
		splitter.execute();

		input.close();

		t.stop(KEY_SPLIT);
		printInfo();

		// Calculate bounding box
		t.start(KEY_COMPUTE_BBOX);
		if (computeBbox) {
			bbox = OsmUtils.computeBBox(fileInputNodes);

			System.out.println("computed bounds: " + BBoxString.create(bbox));
		}
		t.stop(KEY_COMPUTE_BBOX);

		// Create node tree
		t.start(KEY_NODE_TREE);

		DataTree tree = DataTreeUtil.initNewTree(pathTree, bbox);

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

		t.stop(KEY_NODE_TREE);
		printInfo();

		// Sort ways by first node id
		t.start(KEY_SORT_WAYS);

		OsmIteratorInput inputWays = fileInputWays.createIterator(true,
				includeMetadata);

		WaysSorterByFirstNodeId waysSorter = new ThreadedWaysSorterByFirstNodeId(
				inputWays.getIterator(), pathWaysByNodes, outputConfigWays);
		waysSorter.execute();

		inputWays.close();

		t.stop(KEY_SORT_WAYS);

		// Map ways to tree
		t.start(KEY_MAP_WAYS);

		OsmIteratorInput inputNodes = fileInputNodes.createIterator(true,
				includeMetadata);

		WaysToTreeMapper waysMapper = new ThreadedWaysToTreeMapper(
				inputNodes.getIterator(), pathTree, pathWaysByNodes,
				outputFormat, fileNamesInitialWays, outputConfigTree);
		waysMapper.execute();

		inputNodes.close();

		if (!keepWaysByNodes) {
			FileUtils.deleteDirectory(pathWaysByNodes.toFile());
		}

		t.stop(KEY_MAP_WAYS);
		printInfo();

		// Find missing way nodes
		t.start(KEY_FIND_MISSING_WAY_NODES);

		MissingWayNodesFinder wayNodesFinder = new ThreadedMissingWayNodesFinder(
				pathTree, pathTree, pathTree, fileNamesInitialNodes,
				fileNamesInitialWays, fileNamesMissingWayNodeIds, outputFormat,
				outputFormat);
		wayNodesFinder.execute();

		t.stop(KEY_FIND_MISSING_WAY_NODES);
		printInfo();

		// Extract missing way nodes
		t.start(KEY_EXTRACT_MISSING_WAY_NODES);

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

		t.stop(KEY_EXTRACT_MISSING_WAY_NODES);
		printInfo();

		// Distribute ways
		t.start(KEY_DISTRIBUTE_WAYS);

		WaysDistributor waysDistributor = new ThreadedWaysDistributor(pathTree,
				fileNamesInitialNodes, fileNamesMissingNodes,
				fileNamesInitialWays, fileNamesDistributedWays,
				fileNamesDistributedNodes, outputFormat, outputFormat,
				outputConfigTree);
		waysDistributor.execute();

		t.stop(KEY_DISTRIBUTE_WAYS);
		printInfo();

		// Merge nodes
		t.start(KEY_MERGE_NODES);

		List<String> fileNamesSortedNodes = new ArrayList<>();
		List<String> fileNamesUnsortedNodes = new ArrayList<>();
		fileNamesSortedNodes.add(fileNamesInitialNodes);
		fileNamesSortedNodes.add(fileNamesMissingNodes);
		fileNamesUnsortedNodes.add(fileNamesDistributedNodes);
		TreeFilesMerger nodesMerger = new ThreadedTreeFilesMerger(pathTree,
				fileNamesSortedNodes, fileNamesUnsortedNodes,
				fileNamesFinalNodes, outputFormat, outputConfigTreeFinal, true);
		nodesMerger.execute();

		t.stop(KEY_MERGE_NODES);
		printInfo();

		// Merge ways
		t.start(KEY_MERGE_WAYS);

		List<String> fileNamesSortedWays = new ArrayList<>();
		List<String> fileNamesUnsortedWays = new ArrayList<>();
		fileNamesUnsortedWays.add(fileNamesInitialWays);
		fileNamesUnsortedWays.add(fileNamesDistributedWays);
		TreeFilesMerger waysMerger = new ThreadedTreeFilesMerger(pathTree,
				fileNamesSortedWays, fileNamesUnsortedWays, fileNamesFinalWays,
				outputFormat, outputConfigTreeFinal, true);
		waysMerger.execute();

		t.stop(KEY_MERGE_WAYS);
		printInfo();

		// Separate relations
		t.start(KEY_SEPARATE_RELATIONS);

		RelationsSeparator separator = new RelationsSeparator(
				fileInputRelations, pathSimpleRelations, pathComplexRelations,
				outputConfigRelations);
		separator.execute();

		t.stop(KEY_SEPARATE_RELATIONS);
		printInfo();

		// Split relations and collect members
		t.start(KEY_SPLIT_RELATIONS);

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

		if (!keepRelations) {
			Files.delete(pathSimpleRelations);
			Files.delete(pathComplexRelations);
		}

		t.stop(KEY_SPLIT_RELATIONS);
		printInfo();

		// Distribute relations
		t.start(KEY_DISTRIBUTE_RELATIONS);

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

		t.stop(KEY_DISTRIBUTE_RELATIONS);
		printInfo();

		// Sort non-tree relations
		t.start(KEY_SORT_RELATIONS);

		NonTreeRelationsSplitter nonTreeSplitter = new NonTreeRelationsSplitter(
				pathSimpleRelationsNonTree, pathComplexRelationsNonTree,
				pathSimpleRelationsNonTreeBboxes,
				pathComplexRelationsNonTreeBboxes, pathSimpleRelationsDir,
				pathComplexRelationsDir, pathSimpleRelationsSorted,
				pathComplexRelationsSorted, outputFormat,
				outputConfigRelations, pathSimpleRelationsSortedBboxes,
				pathComplexRelationsSortedBboxes, maxMembersSimple,
				maxMembersComplex);
		nonTreeSplitter.execute();

		if (!keepRelationBatches) {
			FileUtils.deleteDirectory(pathSimpleRelationsDir.toFile());
			FileUtils.deleteDirectory(pathComplexRelationsDir.toFile());
		}

		t.stop(KEY_SORT_RELATIONS);

		// Clean up
		t.start(KEY_CLEAN_UP);

		if (!keepNonTreeRelations) {
			Files.delete(pathSimpleRelationsNonTree);
			Files.delete(pathComplexRelationsNonTree);
			Files.delete(pathSimpleRelationsNonTreeBboxes);
			Files.delete(pathComplexRelationsNonTreeBboxes);
		}

		if (!keepSplittedNodes) {
			Files.delete(pathNodes);
		}
		if (!keepSplittedWays) {
			Files.delete(pathWays);
		}
		if (!keepSplittedRelations) {
			Files.delete(pathRelations);
		}

		t.stop(KEY_CLEAN_UP);

		t.stop(KEY_TOTAL);
		printInfo();
	}

	public void printInfo()
	{
		String[] keys = new String[] { KEY_TOTAL, KEY_SPLIT, KEY_COMPUTE_BBOX,
				KEY_NODE_TREE, KEY_SORT_WAYS, KEY_MAP_WAYS,
				KEY_FIND_MISSING_WAY_NODES, KEY_EXTRACT_MISSING_WAY_NODES,
				KEY_DISTRIBUTE_WAYS, KEY_MERGE_NODES, KEY_MERGE_WAYS,
				KEY_SEPARATE_RELATIONS, KEY_SPLIT_RELATIONS,
				KEY_DISTRIBUTE_RELATIONS, KEY_SORT_RELATIONS, KEY_CLEAN_UP };

		for (String key : keys) {
			System.out.println(String.format("%s: %s", key, t.htime(key)));
		}
	}

}

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

package de.topobyte.osm4j.extra.datatree;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.Merge;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeMaxNodes extends BaseNodeTreeCreator
{

	private static final String OPTION_MAX_NODES = "max_nodes";

	private static final String OPTION_PRE_SPLIT_FORMAT = "pre_split_format";
	private static final String OPTION_PRE_SPLIT_DATA = "pre_split_data";
	private static final String OPTION_PRE_SPLIT_MAX = "pre_split_max";

	private static final String SUBDIR_EXTRA = "extra";

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeMaxNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeMaxNodes task = new CreateNodeTreeMaxNodes();

		task.setup(args);

		task.readMetadata = true;

		task.init();

		task.initTree();

		task.initOutputs();

		task.run();

		task.finish();
	}

	private int maxNodes;

	private int preSplitMaxNodes;
	private FileFormat preSplitFormat;
	private String preSplitPath;

	public CreateNodeTreeMaxNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");

		OptionHelper.add(options, OPTION_PRE_SPLIT_FORMAT, true, false, "file format of pre split data");
		OptionHelper.add(options, OPTION_PRE_SPLIT_DATA, true, false, "path to pre split data");
		OptionHelper.add(options, OPTION_PRE_SPLIT_MAX, true, false, "max nodes per file pre split");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argMaxNodes = line.getOptionValue(OPTION_MAX_NODES);

		maxNodes = Integer.parseInt(argMaxNodes);
		if (maxNodes < 1) {
			System.out.println("Please specify a max nodes >= 1");
			System.exit(1);
		}

		boolean hasPreSplitFormat = line.hasOption(OPTION_PRE_SPLIT_FORMAT);
		boolean hasPreSplitData = line.hasOption(OPTION_PRE_SPLIT_DATA);
		boolean hasPreSplitMax = line.hasOption(OPTION_PRE_SPLIT_MAX);

		if (hasPreSplitData || hasPreSplitFormat || hasPreSplitMax) {
			if (!hasPreSplitData || !hasPreSplitFormat || !hasPreSplitMax) {
				System.out
						.println("Please specifiy all or none options for pre splitting");
				System.exit(1);
			}

			preSplitMaxNodes = Integer.parseInt(line
					.getOptionValue(OPTION_PRE_SPLIT_MAX));
			if (preSplitMaxNodes < 1) {
				System.out.println("Please specify a pre split max nodes >= 1");
				System.exit(1);
			}

			String preSplitFormatName = line
					.getOptionValue(OPTION_PRE_SPLIT_FORMAT);
			preSplitFormat = FileFormat.parseFileFormat(preSplitFormatName);
			if (outputFormat == null) {
				System.out.println("invalid pre split format");
				System.out.println("please specify one of: "
						+ FileFormat.getHumanReadableListOfSupportedFormats());
				System.exit(1);
			}

			preSplitPath = line.getOptionValue(OPTION_PRE_SPLIT_DATA);
		}
	}

	@Override
	protected void init() throws IOException
	{
		super.init();
	}

	@Override
	protected void initTree() throws IOException
	{
		super.initTree();

		if (preSplitPath != null) {
			System.out.println("Splitting tree with warm up data");
			TreeSplitter splitter = new TreeSplitter(tree);
			InputStream input = new FileInputStream(preSplitPath);
			OsmIterator iterator = OsmIoUtils.setupOsmIterator(input,
					preSplitFormat, false);
			splitter.split(iterator, preSplitMaxNodes);
			System.out.println("Number of leafs: " + tree.getLeafs().size());
		}

		tree.print();
	}

	protected void initOutputs() throws IOException
	{
		List<Node> leafs = tree.getLeafs();
		for (Node leaf : leafs) {
			init(leaf);
		}
	}

	@Override
	protected void run() throws IOException
	{
		// Run through input and distribute to leafs, splitting tree nodes on
		// the way when they reach the maximum data size
		long t1 = System.currentTimeMillis();
		super.run();
		// Close input and output streams
		super.finish();

		// Data associated with inner tree nodes needs to be distributed to
		// leafs.
		long t2 = System.currentTimeMillis();
		distributeInnerNodeData();

		// Merge data files of tree leafs with multiple files (as a result of
		// the distribution of data from inner nodes)
		long t3 = System.currentTimeMillis();
		merge();

		// The merging may result in size limit violations, split such data
		// files
		long t4 = System.currentTimeMillis();
		splitLargeLeafs();
		long t5 = System.currentTimeMillis();

		// Check for mergeable leafs
		mergeReport();

		System.out.println("Total time: " + (t5 - t1) / 1000);
		System.out.println("Time for main step: " + (t2 - t1) / 1000);
		System.out.println("Time for distributing inner node data: "
				+ (t3 - t2) / 1000);
		System.out.println("Time for merging leaf data: " + (t4 - t3) / 1000);
		System.out.println("Time for splitting large leafs: " + (t5 - t4)
				/ 1000);
	}

	@Override
	protected void finish()
	{

	}

	@Override
	protected void handle(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			if (leaf.getEnvelope().contains(node.getLongitude(),
					node.getLatitude())) {
				Output output = outputs.get(leaf);
				output.incrementCounter();
				output.getOsmOutput().write(node);
				if (output.getCount() > maxNodes) {
					split(leaf);
				}
			}
		}
	}

	private void split(Node leaf) throws IOException
	{
		leaf.split();
		Node left = leaf.getLeft();
		Node right = leaf.getRight();

		System.out.println("Splitting: " + leaf.getEnvelope());
		System.out.println(String.format("%s -> %s %s",
				Long.toHexString(leaf.getPath()),
				Long.toHexString(left.getPath()),
				Long.toHexString(right.getPath())));

		Output output = outputs.get(leaf);
		output.getOsmOutput().complete();
		output.getOutputStream().close();

		init(left);
		init(right);
	}

	/*
	 * Distributing data from inner nodes to leafs
	 */

	private void distributeInnerNodeData() throws IOException
	{
		List<Node> innerNodes = tree.getInner();
		List<Node> innerNodesWithData = new ArrayList<>();
		for (int i = 0; i < innerNodes.size(); i++) {
			Node node = innerNodes.get(i);
			if (outputs.get(node) != null) {
				innerNodesWithData.add(node);
			}
		}

		System.out.println("Number of inner tree nodes with data: "
				+ innerNodesWithData.size());

		for (int i = 0; i < innerNodesWithData.size(); i++) {
			Node node = innerNodesWithData.get(i);
			List<Node> leafs = tree.getLeafs(node);
			System.out.println(String.format(
					"Distributing [%d/%d]: %s. Leafs: %d", i + 1,
					innerNodesWithData.size(),
					Long.toHexString(node.getPath()), leafs.size()));

			distributeToLeafs(node, leafs);
		}
	}

	private void distributeToLeafs(Node inner, List<Node> leafs)
			throws IOException
	{
		Map<Node, Output> extraOutputs = new HashMap<>();

		for (Node leaf : leafs) {
			Output output = initExtraOutput(inner, leaf);
			extraOutputs.put(leaf, output);
		}

		Output output = outputs.get(inner);

		InputStream input = new FileInputStream(output.getFile().toFile());
		input = new BufferedInputStream(input);
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, outputFormat,
				writeMetadata);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				continue;
			}
			OsmNode node = (OsmNode) container.getEntity();
			double lon = node.getLongitude();
			double lat = node.getLatitude();

			List<Node> targetLeafs = tree.query(inner, lon, lat);
			for (Node targetLeaf : targetLeafs) {
				Output extraOutput = extraOutputs.get(targetLeaf);
				extraOutput.incrementCounter();
				extraOutput.getOsmOutput().write(node);
			}
		}

		for (Output extraOutput : extraOutputs.values()) {
			close(extraOutput);
		}

		input.close();

		System.out.println("Deleting " + Long.toHexString(inner.getPath()));
		Files.delete(output.getFile());
		Files.delete(output.getFile().getParent());
	}

	private Output initExtraOutput(Node inner, Node leaf) throws IOException
	{
		String dirname = Long.toHexString(leaf.getPath());
		String filename = Long.toHexString(inner.getPath())
				+ OsmIoUtils.extension(outputFormat);
		Path dirLeaf = dirOutput.resolve(dirname);
		Path dirExtra = dirLeaf.resolve(SUBDIR_EXTRA);

		Files.createDirectories(dirExtra);
		Path file = dirExtra.resolve(filename);

		OutputStream os = new FileOutputStream(file.toFile());
		OutputStream bos = new BufferedOutputStream(os);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(bos,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		Output output = new Output(file, bos, osmOutput);

		Envelope box = leaf.getEnvelope();
		osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box.getMaxY(),
				box.getMinY()));

		return output;
	}

	/*
	 * Merging data from inner nodes into leafs
	 */

	private void merge() throws IOException
	{
		List<Node> leafs = tree.getLeafs();
		List<Node> leafsToMerge = new ArrayList<>();
		Map<Node, List<File>> leafToExtra = new HashMap<>();

		for (Node leaf : leafs) {
			Output output = outputs.get(leaf);
			Path extra = output.getFile().getParent().resolve(SUBDIR_EXTRA);

			if (!Files.exists(extra)) {
				continue;
			}

			List<File> extraFiles = Arrays.asList(extra.toFile().listFiles());
			if (extraFiles.isEmpty()) {
				continue;
			}

			leafsToMerge.add(leaf);
			leafToExtra.put(leaf, extraFiles);
		}

		for (int i = 0; i < leafsToMerge.size(); i++) {
			Node leaf = leafsToMerge.get(i);
			System.out.println(String.format("Merging [%d/%d]: %s", i + 1,
					leafsToMerge.size(), Long.toHexString(leaf.getPath())));
			merge(leaf);
		}
	}

	private void merge(Node leaf) throws IOException
	{
		Output output = outputs.get(leaf);
		Path finalOutput = output.getFile();
		Path extra = finalOutput.getParent().resolve(SUBDIR_EXTRA);

		if (!Files.exists(extra)) {
			return;
		}

		List<File> extraFiles = Arrays.asList(extra.toFile().listFiles());
		if (extraFiles.isEmpty()) {
			return;
		}

		Path intermediateOutput = finalOutput.resolveSibling("intermediate"
				+ OsmIoUtils.extension(outputFormat));
		Files.move(finalOutput, intermediateOutput);

		OutputStream os = new FileOutputStream(finalOutput.toFile());
		OutputStream bos = new BufferedOutputStream(os);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(bos,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		CountingOsmOutputStream countingOsmOutput = new CountingOsmOutputStream(
				osmOutput);

		List<OsmIterator> inputs = new ArrayList<>();
		List<InputStream> inputStreams = new ArrayList<>();

		List<File> mergeFiles = new ArrayList<>();
		mergeFiles.add(intermediateOutput.toFile());
		mergeFiles.addAll(extraFiles);

		System.out
				.println(String.format("Merging %d files", mergeFiles.size()));

		for (File file : mergeFiles) {
			InputStream fis = new FileInputStream(file);
			InputStream bis = new BufferedInputStream(fis);
			OsmIterator iterator = OsmIoUtils.setupOsmIterator(bis,
					outputFormat, writeMetadata);
			inputs.add(iterator);
			inputStreams.add(bis);
		}

		Merge merge = new Merge(countingOsmOutput, inputs);
		merge.run();

		for (InputStream stream : inputStreams) {
			stream.close();
		}

		bos.close();

		for (File file : mergeFiles) {
			file.delete();
		}
		Files.delete(extra);

		output.setCount(countingOsmOutput.getNumNodes());
	}

	/*
	 * Splitting leafs that are too large after merging
	 */

	private Deque<NodeOutput> largeOutputs = new LinkedList<>();

	private void splitLargeLeafs() throws IOException
	{
		for (Node leaf : tree.getLeafs()) {
			NodeOutput output = outputs.get(leaf);
			if (output.getCount() > maxNodes) {
				largeOutputs.add(output);
			}
		}

		System.out.println("Number of large leafs: " + largeOutputs.size());

		while (!largeOutputs.isEmpty()) {
			NodeOutput output = largeOutputs.pop();
			split(output);

			if (!largeOutputs.isEmpty()) {
				System.out.println("Remaining number of large leafs: "
						+ largeOutputs.size());
			}
		}
	}

	private void split(NodeOutput output) throws IOException
	{
		Node leaf = output.getNode();
		leaf.split();
		Node left = leaf.getLeft();
		Node right = leaf.getRight();

		System.out.println("Splitting: " + leaf.getEnvelope());
		System.out.println(String.format("%s -> %s %s",
				Long.toHexString(leaf.getPath()),
				Long.toHexString(left.getPath()),
				Long.toHexString(right.getPath())));

		NodeOutput leftOutput = init(left);
		NodeOutput rightOutput = init(right);

		Path path = outputs.get(leaf).getFile();
		InputStream fis = new FileInputStream(path.toFile());
		InputStream bis = new BufferedInputStream(fis);
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(bis, outputFormat,
				writeMetadata);

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			Side side = leaf.side(node.getLongitude(), node.getLatitude());
			if (side == Side.LEFT || side == Side.ON) {
				leftOutput.getOsmOutput().write(node);
				leftOutput.incrementCounter();
			}
			if (side == Side.RIGHT || side == Side.ON) {
				rightOutput.getOsmOutput().write(node);
				rightOutput.incrementCounter();
			}
		}

		close(leftOutput);
		close(rightOutput);

		System.out.println(String.format("Splitted %d nodes to %d / %d",
				output.getCount(), leftOutput.getCount(),
				rightOutput.getCount()));

		if (leftOutput.getCount() > maxNodes) {
			largeOutputs.add(leftOutput);
		}
		if (rightOutput.getCount() > maxNodes) {
			largeOutputs.add(rightOutput);
		}

		bis.close();
		Files.delete(path);
		Files.delete(path.getParent());
	}

	/*
	 * Report underfilled tree node siblings that could be merged
	 */

	private void mergeReport()
	{
		List<Node> leafs = tree.getLeafs();

		System.out.println("inner nodes: " + tree.getInner().size());
		System.out.println("leafs: " + leafs.size());

		int possibleMerges = 0;

		Map<Node, Long> counts = new HashMap<>();
		for (Node leaf : leafs) {
			NodeOutput output = outputs.get(leaf);
			counts.put(leaf, output.getCount());
		}

		PriorityQueue<Node> check = new PriorityQueue<>(2,
				new Comparator<Node>() {

					@Override
					public int compare(Node o1, Node o2)
					{
						return Integer.compare(o2.getLevel(), o1.getLevel());
					}
				});
		Set<Node> done = new HashSet<>();
		check.addAll(leafs);
		while (!check.isEmpty()) {
			Node leaf = check.poll();
			if (leaf.getParent() == null) {
				continue;
			}
			if (done.contains(leaf)) {
				continue;
			}
			Node sibling = leaf.getSibling();
			done.add(sibling);

			if (!counts.containsKey(sibling)) {
				continue;
			}

			long sum = counts.get(leaf) + counts.get(sibling);
			System.out.println(String.format("%s %s: %d + %d = %d",
					Long.toHexString(leaf.getPath()),
					Long.toHexString(sibling.getPath()), counts.get(leaf),
					counts.get(sibling), sum));
			Node parent = leaf.getParent();
			counts.put(parent, sum);
			if (sum < maxNodes) {
				check.add(parent);
				possibleMerges += 1;
			}
		}

		System.out.println(String.format("Could perform %d merges",
				possibleMerges));
	}

}

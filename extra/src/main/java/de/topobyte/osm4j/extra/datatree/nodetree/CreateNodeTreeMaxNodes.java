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

package de.topobyte.osm4j.extra.datatree.nodetree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmIteratorInput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeMaxNodes extends
		AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES = "filenames";
	private static final String OPTION_MAX_NODES = "max_nodes";

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeMaxNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeMaxNodes task = new CreateNodeTreeMaxNodes();

		task.setup(args);

		task.init();

		task.initTree();
	}

	protected int maxNodes;
	protected String pathOutput;
	protected String fileNames;

	protected Path dirOutput;
	protected Map<Node, NodeOutput> outputs = new HashMap<>();

	protected Envelope envelope;
	protected DataTree tree;

	public CreateNodeTreeMaxNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
		OptionHelper.add(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
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

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
	}

	private void init() throws IOException
	{
		dirOutput = Paths.get(pathOutput);
		if (!Files.exists(dirOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(dirOutput);
		}
		if (!Files.isDirectory(dirOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (dirOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}

		OsmIteratorInput input = getOsmFileInput().createIterator(false);
		OsmIterator iterator = input.getIterator();

		if (!iterator.hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}

		OsmBounds bounds = iterator.getBounds();
		System.out.println("bounds: " + bounds);

		input.close();

		envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());
	}

	private void initTree() throws IOException
	{
		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutput.toFile(), bbox);

		tree = new DataTree(envelope);

		tree.getRoot().split(SPLIT_INITIAL);
		NodeTreeDistributer initialDistributer = new NodeTreeDistributer(tree,
				dirOutput, tree.getRoot(), getOsmFile().getPath().toFile(),
				maxNodes, fileNames, inputFormat, outputFormat, pbfConfig,
				tboConfig, writeMetadata);
		initialDistributer.execute();

		Deque<NodeTreeDistributer> check = new LinkedList<>();
		check.add(initialDistributer);

		int iteration = 0;

		while (!check.isEmpty()) {
			iteration++;
			System.out.println(String.format("Iteration %d", iteration));

			Map<Node, Path> paths = new HashMap<>();
			List<Node> largeNodes = new ArrayList<>();
			for (NodeTreeDistributer distributer : check) {
				for (Node node : tree.getLeafs(distributer.getHead())) {
					long count = distributer.getCounters().get(node.getPath());
					if (count <= maxNodes) {
						continue;
					}
					System.out.println(String.format(
							"Node %s has too many nodes: %d",
							Long.toHexString(node.getPath()), count));
					largeNodes.add(node);
					paths.put(node, distributer.getOutputs().get(node)
							.getFile());
				}
			}
			check.clear();

			System.out.println(String.format(
					"Iteration %d: there are %d large nodes", iteration,
					largeNodes.size()));

			for (Node node : largeNodes) {
				Path path = paths.get(node);
				System.out.println(String.format("Splitting again: node %s",
						Long.toHexString(node.getPath())));
				node.split(SPLIT_ITERATION);
				NodeTreeDistributer distributer = new NodeTreeDistributer(tree,
						dirOutput, node, path.toFile(), maxNodes, fileNames,
						outputFormat, outputFormat, pbfConfig, tboConfig,
						writeMetadata);
				distributer.execute();
				check.add(distributer);

				Files.delete(path);
				Files.delete(path.getParent());
			}
		}
	}

}

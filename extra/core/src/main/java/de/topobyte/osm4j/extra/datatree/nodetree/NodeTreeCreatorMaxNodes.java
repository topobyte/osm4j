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
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmInputAccessFactory;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.datatree.nodetree.count.NodeTreeLeafCounter;
import de.topobyte.osm4j.extra.datatree.nodetree.count.NodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributor;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class NodeTreeCreatorMaxNodes
{

	private OsmInputAccessFactory inputFactory;
	private DataTreeOutputFactory outputFactory;

	private int maxNodes;
	private int splitInitial;
	private int splitIteration;

	private Path dirOutput;
	private String fileNames;

	private OsmOutputConfig outputConfig;

	private DataTree tree;

	private NodeTreeLeafCounterFactory counterFactory;
	private NodeTreeDistributorFactory distributorFactory;

	public NodeTreeCreatorMaxNodes(DataTree tree,
			OsmInputAccessFactory inputFactory,
			DataTreeOutputFactory outputFactory, int maxNodes,
			int splitInitial, int splitIteration, Path dirOutput,
			String fileNames, OsmOutputConfig outputConfig,
			NodeTreeLeafCounterFactory counterFactory,
			NodeTreeDistributorFactory distributorFactory)
	{
		this.tree = tree;
		this.inputFactory = inputFactory;
		this.outputFactory = outputFactory;
		this.maxNodes = maxNodes;
		this.splitInitial = splitInitial;
		this.splitIteration = splitIteration;
		this.dirOutput = dirOutput;
		this.fileNames = fileNames;
		this.outputConfig = outputConfig;
		this.counterFactory = counterFactory;
		this.distributorFactory = distributorFactory;
	}

	private Deque<NodeTreeLeafCounter> check = new LinkedList<>();

	public void buildTree() throws IOException
	{
		DataTreeFiles treeFiles = new DataTreeFiles(dirOutput, fileNames);

		tree.getRoot().split(splitInitial);

		countAndDistribute(tree.getRoot(), inputFactory);

		int iteration = 0;

		while (!check.isEmpty()) {
			iteration++;
			System.out.println(String.format("Iteration %d", iteration));

			List<Node> largeNodes = new ArrayList<>();
			for (NodeTreeLeafCounter counter : check) {
				for (Node node : tree.getLeafs(counter.getHead())) {
					long count = counter.getCounters().get(node.getPath());
					if (count <= maxNodes) {
						continue;
					}
					System.out.println(String.format(
							"Node %s has too many nodes: %d",
							Long.toHexString(node.getPath()), count));
					largeNodes.add(node);
				}
			}
			check.clear();

			System.out.println(String.format(
					"Iteration %d: there are %d large nodes", iteration,
					largeNodes.size()));

			for (Node node : largeNodes) {
				Path path = treeFiles.getPath(node);
				System.out.println(String.format("Splitting again: node %s",
						Long.toHexString(node.getPath())));
				node.split(splitIteration);

				countAndDistribute(node,
						new OsmFileInput(path, outputConfig.getFileFormat()));

				Files.delete(path);
				Files.delete(path.getParent());
			}
		}
	}

	private void countAndDistribute(Node node,
			OsmInputAccessFactory inputFactory) throws IOException
	{
		OsmIteratorInput input = inputFactory.createIterator(false, false);
		NodeTreeLeafCounter counter = counterFactory.createLeafCounter(tree,
				input.getIterator(), node);
		try {
			counter.execute();
		} finally {
			input.close();
		}

		DataTreeUtil.mergeUnderfilledSiblings(tree, node, maxNodes,
				counter.getCounters());

		input = inputFactory.createIterator(true,
				outputConfig.isWriteMetadata());
		NodeTreeDistributor distributor = distributorFactory.createDistributor(
				tree, node, input.getIterator(), outputFactory);
		try {
			distributor.execute();
		} finally {
			input.close();
		}

		check.add(counter);
	}

}

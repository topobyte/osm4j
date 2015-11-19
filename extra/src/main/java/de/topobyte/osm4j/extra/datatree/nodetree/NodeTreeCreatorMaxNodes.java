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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmInputAccessFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class NodeTreeCreatorMaxNodes
{

	private OsmInputAccessFactory inputFactory;

	private int maxNodes;
	private int splitInitial;
	private int splitIteration;

	private Path dirOutput;
	private String fileNames;

	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata;

	private Envelope envelope;
	private DataTree tree;

	public NodeTreeCreatorMaxNodes(OsmInputAccessFactory inputFactory,
			int maxNodes, int splitInitial, int splitIteration, Path dirOutput,
			String fileNames, FileFormat outputFormat, PbfConfig pbfConfig,
			TboConfig tboConfig, boolean writeMetadata)
	{
		this.inputFactory = inputFactory;
		this.maxNodes = maxNodes;
		this.splitInitial = splitInitial;
		this.splitIteration = splitIteration;
		this.dirOutput = dirOutput;
		this.fileNames = fileNames;

		this.outputFormat = outputFormat;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
		this.writeMetadata = writeMetadata;
	}

	public void init() throws IOException
	{
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

		OsmIteratorInput input = inputFactory.createIterator(false);
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

	public void buildTree() throws IOException
	{
		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutput.toFile(), bbox);

		tree = new DataTree(envelope);

		tree.getRoot().split(splitInitial);
		NodeTreeDistributer initialDistributer = new NodeTreeDistributer(tree,
				inputFactory, dirOutput, tree.getRoot(), maxNodes, fileNames,
				outputFormat, pbfConfig, tboConfig, writeMetadata);
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
				node.split(splitIteration);
				NodeTreeDistributer distributer = new NodeTreeDistributer(tree,
						new OsmFileInput(path, outputFormat), dirOutput, node,
						maxNodes, fileNames, outputFormat, pbfConfig,
						tboConfig, writeMetadata);
				distributer.execute();
				check.add(distributer);

				Files.delete(path);
				Files.delete(path.getParent());
			}
		}
	}

}

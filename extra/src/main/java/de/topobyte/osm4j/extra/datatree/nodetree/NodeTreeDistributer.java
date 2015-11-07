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

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.largescalefileio.ClosingFileOutputStream;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.ClosingFileOutputStreamPool;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class NodeTreeDistributer
{

	private DataTree tree;
	private Path dirOutput;
	private Node head;

	private File fileInput;
	private int maxNodes;
	private String fileNames;
	private FileFormat inputFormat;
	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata;

	private ClosingFileOutputStreamFactory outputStreamFactory = new ClosingFileOutputStreamPool();
	private int idFactory = 0;

	private Map<Node, NodeOutput> outputs = new HashMap<>();

	public NodeTreeDistributer(DataTree tree, Path dirOutput, Node head,
			File fileInput, int maxNodes, String filenames,
			FileFormat inputFormat, FileFormat outputFormat,
			PbfConfig pbfConfig, TboConfig tboConfig, boolean writeMetadata)
	{
		this.tree = tree;
		this.dirOutput = dirOutput;
		this.head = head;
		this.fileInput = fileInput;
		this.maxNodes = maxNodes;
		this.fileNames = filenames;
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
		this.writeMetadata = writeMetadata;
	}

	public Node getHead()
	{
		return head;
	}

	public Map<Node, NodeOutput> getOutputs()
	{
		return outputs;
	}

	public void execute() throws IOException
	{
		countLeafNodes();

		mergeUnderfilledSiblings();

		initOutputs();

		distributeNodes();

		finish();
	}

	private TLongLongMap counters = new TLongLongHashMap();

	public TLongLongMap getCounters()
	{
		return counters;
	}

	private void countLeafNodes() throws IOException
	{
		NodeProgress counter = new NodeProgress();
		counter.printTimed(1000);

		InputStream input = new BufferedInputStream(new FileInputStream(
				fileInput));
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				false);

		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				OsmNode node = (OsmNode) entityContainer.getEntity();
				findLeafsAndIncrementCounters(node);
				counter.increment();
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}

		counter.stop();
		input.close();
	}

	private void findLeafsAndIncrementCounters(OsmNode node)
	{
		List<Node> leafs = tree.query(head, node.getLongitude(),
				node.getLatitude());
		for (Node leaf : leafs) {
			long path = leaf.getPath();
			counters.put(path, counters.get(path) + 1);
		}
	}

	private void mergeUnderfilledSiblings()
	{
		List<Node> inner = tree.getInner(head);
		List<Node> leafs = tree.getLeafs(head);

		System.out.println("Before merging underfilled siblings:");
		System.out.println("inner nodes: " + inner.size());
		System.out.println("leafs: " + leafs.size());

		TObjectLongMap<Node> counts = new TObjectLongHashMap<>();
		for (Node leaf : leafs) {
			long count = counters.get(leaf.getPath());
			counts.put(leaf, count);
		}

		List<Node> check = new ArrayList<>(inner);
		Collections.sort(check, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2)
			{
				return Integer.compare(o2.getLevel(), o1.getLevel());
			}
		});
		for (Node node : check) {
			if (!node.getLeft().isLeaf() || !node.getRight().isLeaf()) {
				continue;
			}
			long sum = counts.get(node.getLeft()) + counts.get(node.getRight());
			if (sum < maxNodes) {
				node.melt();
				counts.put(node, sum);
			}
		}

		System.out.println("After:");
		System.out.println("inner nodes: " + tree.getInner(head).size());
		System.out.println("leafs: " + tree.getLeafs(head).size());
	}

	private void initOutputs() throws IOException
	{
		List<Node> leafs = tree.getLeafs(head);
		for (Node leaf : leafs) {
			init(leaf);
		}
	}

	private NodeOutput init(Node leaf) throws IOException
	{
		String dirname = Long.toHexString(leaf.getPath());
		Path dir = dirOutput.resolve(dirname);
		Files.createDirectories(dir);
		Path file = dir.resolve(fileNames);

		OutputStream os = new ClosingFileOutputStream(outputStreamFactory,
				file.toFile(), idFactory++);
		OutputStream bos = new BufferedOutputStream(os);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(bos,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		NodeOutput output = new NodeOutput(leaf, file, bos, osmOutput);
		outputs.put(leaf, output);

		Envelope box = leaf.getEnvelope();
		osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box.getMaxY(),
				box.getMinY()));

		return output;
	}

	private void distributeNodes() throws IOException
	{
		NodeProgress counter = new NodeProgress();
		counter.printTimed(1000);

		InputStream input = new BufferedInputStream(new FileInputStream(
				fileInput));
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);

		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				OsmNode node = (OsmNode) entityContainer.getEntity();
				writeToLeafs(node);
				counter.increment();
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}
		counter.stop();
		input.close();
	}

	private void writeToLeafs(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(head, node.getLongitude(),
				node.getLatitude());
		for (Node leaf : leafs) {
			NodeOutput output = outputs.get(leaf);
			output.getOsmOutput().write(node);
		}
	}

	private void finish() throws IOException
	{
		for (Output output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

}

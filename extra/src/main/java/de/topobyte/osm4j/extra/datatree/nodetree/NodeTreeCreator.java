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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class NodeTreeCreator
{

	private Path dirOutput;
	private String fileNames;

	private OsmIterator input;
	private DataTree tree;

	private OsmOutputConfig outputConfig;

	private Map<Node, NodeOutput> outputs = new HashMap<>();
	private ClosingFileOutputStreamFactory outputStreamFactory = new SimpleClosingFileOutputStreamFactory();

	private NodeProgress counter = new NodeProgress();

	public NodeTreeCreator(OsmIterator input, Path dirOutput, String fileNames,
			OsmOutputConfig outputConfig)
	{
		this.input = input;
		this.dirOutput = dirOutput;
		this.fileNames = fileNames;
		this.outputConfig = outputConfig;
	}

	public void initNewTree() throws IOException
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

		if (!input.hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}

		OsmBounds bounds = input.getBounds();
		System.out.println("bounds: " + bounds);

		Envelope envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());

		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutput.toFile(), bbox);

		tree = new DataTree(envelope);
	}

	public void openExistingTree() throws IOException
	{
		if (!Files.exists(dirOutput)) {
			System.out.println("Output path does not exist");
			System.exit(1);
		}
		if (!Files.isDirectory(dirOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}

		tree = DataTreeOpener.open(dirOutput.toFile());
	}

	public DataTree getTree()
	{
		return tree;
	}

	public void execute() throws IOException
	{
		initOutputs();

		run();

		finish();
	}

	protected void initOutputs() throws IOException
	{
		List<Node> leafs = tree.getLeafs();
		for (Node leaf : leafs) {
			init(leaf);
		}
	}

	protected NodeOutput init(Node leaf) throws IOException
	{
		String dirname = Long.toHexString(leaf.getPath());
		Path dir = dirOutput.resolve(dirname);
		Files.createDirectories(dir);
		Path file = dir.resolve(fileNames);

		System.out.println(file + ": " + leaf.getEnvelope());
		OutputStream os = outputStreamFactory.create(file.toFile());
		OutputStream bos = new BufferedOutputStream(os);
		OsmOutputStream osmOutput = OsmIoUtils
				.setupOsmOutput(bos, outputConfig);
		NodeOutput output = new NodeOutput(leaf, file, bos, osmOutput);
		outputs.put(leaf, output);

		Envelope box = leaf.getEnvelope();
		osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box.getMaxY(),
				box.getMinY()));

		return output;
	}

	protected void run() throws IOException
	{
		counter.printTimed(1000);
		loop: while (input.hasNext()) {
			EntityContainer entityContainer = input.next();
			switch (entityContainer.getType()) {
			case Node:
				handle((OsmNode) entityContainer.getEntity());
				counter.increment();
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}
		counter.stop();
	}

	protected void handle(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			if (leaf.getEnvelope().contains(node.getLongitude(),
					node.getLatitude())) {
				OsmStreamOutput output = outputs.get(leaf);
				output.getOsmOutput().write(node);
			}
		}
	}

	protected void finish() throws IOException
	{
		for (OsmStreamOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

}

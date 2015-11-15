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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class BaseNodeTreeCreator extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES = "filenames";

	protected OsmIterator inputIterator;

	protected String pathOutput;
	protected String fileNames;

	protected Path dirOutput;
	protected Map<Node, NodeOutput> outputs = new HashMap<>();

	protected DataTree tree;
	protected ClosingFileOutputStreamFactory outputStreamFactory = new SimpleClosingFileOutputStreamFactory();

	private NodeProgress counter = new NodeProgress();

	public BaseNodeTreeCreator()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
	}

	@Override
	protected void init() throws IOException
	{
		super.init();
		inputIterator = createIterator();
	}

	protected void initNewTree() throws IOException
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

		if (!inputIterator.hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}

		OsmBounds bounds = inputIterator.getBounds();
		System.out.println("bounds: " + bounds);

		Envelope envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());

		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutput.toFile(), bbox);

		tree = new DataTree(envelope);
	}

	protected void openExistingTree() throws IOException
	{
		dirOutput = Paths.get(pathOutput);
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
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(bos,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
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
		loop: while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
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

	protected abstract void handle(OsmNode node) throws IOException;

	@Override
	protected void finish() throws IOException
	{
		super.finish();

		for (Output output : outputs.values()) {
			close(output);
		}
	}

	protected void close(Output output) throws IOException
	{
		output.getOsmOutput().complete();
		output.getOutputStream().close();
	}

}

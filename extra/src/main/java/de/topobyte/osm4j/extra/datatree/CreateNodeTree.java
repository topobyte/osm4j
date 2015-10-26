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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIterator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTree extends AbstractTaskSingleInputIterator
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";
	private static final String OPTION_SPLIT_DEPTH = "split_depth";

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTree.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTree task = new CreateNodeTree();

		task.setup(args);

		task.readMetadata = true;

		task.init();

		task.initOutputs();

		task.run();

		task.finish();
	}

	private int splitDepth;
	private String pathOutput;
	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private DataTree tree;
	private boolean writeMetadata = true;

	private File dirOutput;
	private Map<Node, Output> outputs = new HashMap<>();

	private class Output
	{

		private OutputStream output;
		private OsmOutputStream osmOutput;

		Output(OutputStream output, OsmOutputStream osmOutput)
		{
			this.output = output;
			this.osmOutput = osmOutput;
		}

	}

	public CreateNodeTree()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_SPLIT_DEPTH, true, true, "how often to split the root node");
		PbfOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String outputFormatName = line.getOptionValue(OPTION_OUTPUT_FORMAT);
		outputFormat = FileFormat.parseFileFormat(outputFormatName);
		if (outputFormat == null) {
			System.out.println("invalid output format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pbfConfig = PbfOptions.parse(line);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		String argSplitDepth = line.getOptionValue(OPTION_SPLIT_DEPTH);

		splitDepth = Integer.parseInt(argSplitDepth);
		if (splitDepth < 0) {
			System.out.println("Please specify a positive split depth");
			System.exit(1);
		}
	}

	@Override
	protected void init() throws FileNotFoundException
	{
		super.init();

		dirOutput = new File(pathOutput);
		if (!dirOutput.exists()) {
			System.out.println("Creating output directory");
			dirOutput.mkdirs();
		}
		if (!dirOutput.isDirectory()) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (dirOutput.list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}

		if (!inputIterator.hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}
	}

	protected void initOutputs() throws IOException
	{
		OsmBounds bounds = inputIterator.getBounds();
		Envelope envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());

		tree = new DataTree(envelope);
		tree.getRoot().split(splitDepth);

		System.out.println("bounds: " + bounds);
		tree.print();

		List<Node> leafs = tree.getLeafs();
		for (Node leaf : leafs) {
			String filename = Integer.toHexString(leaf.getPath()) + ".pbf";
			File file = new File(dirOutput, filename);
			System.out.println(file + ": " + leaf.getEnvelope());
			OutputStream output = new FileOutputStream(file);
			OsmOutputStream osmOutput = setupOsmOutput(output);
			outputs.put(leaf, new Output(output, osmOutput));

			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}
	}

	private OsmOutputStream setupOsmOutput(OutputStream out)
	{
		switch (outputFormat) {
		default:
		case TBO:
			return new TboWriter(out);
		case XML:
			return new OsmXmlOutputStream(out, writeMetadata);
		case PBF:
			PbfWriter pbfWriter = new PbfWriter(out, writeMetadata);
			pbfWriter.setCompression(pbfConfig.getCompression());
			pbfWriter.setUseDense(pbfConfig.isUseDenseNodes());
			return pbfWriter;
		}
	}

	protected void run() throws IOException
	{
		loop: while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
			switch (entityContainer.getType()) {
			case Node:
				handle((OsmNode) entityContainer.getEntity());
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}
	}

	private void handle(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			if (leaf.getEnvelope().contains(node.getLongitude(),
					node.getLatitude())) {
				Output output = outputs.get(leaf);
				output.osmOutput.write(node);
			}
		}
	}

	@Override
	protected void finish() throws IOException
	{
		super.finish();

		for (Output output : outputs.values()) {
			output.osmOutput.complete();
			output.output.close();
		}
	}

}

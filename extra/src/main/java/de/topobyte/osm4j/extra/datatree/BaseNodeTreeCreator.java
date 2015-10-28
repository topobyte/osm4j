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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIterator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class BaseNodeTreeCreator extends
		AbstractTaskSingleInputIterator
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";

	protected String pathOutput;
	protected FileFormat outputFormat;
	protected PbfConfig pbfConfig;
	protected boolean writeMetadata = true;

	protected File dirOutput;
	protected Map<Node, Output> outputs = new HashMap<>();

	protected Envelope envelope;
	protected DataTree tree;

	public BaseNodeTreeCreator()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
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

		OsmBounds bounds = inputIterator.getBounds();
		System.out.println("bounds: " + bounds);

		envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());
	}

	protected void initTree() throws IOException
	{
		String filename = "tree.info";
		File file = new File(dirOutput, filename);

		BBox bbox = new BBox(envelope);
		PrintWriter pw = new PrintWriter(file);
		pw.println("bbox: " + BBoxString.create(bbox));
		pw.close();

		tree = new DataTree(envelope);
	}

	protected Output init(Node leaf) throws IOException
	{
		String filename = Integer.toHexString(leaf.getPath())
				+ Util.extension(outputFormat);
		File file = new File(dirOutput, filename);
		System.out.println(file + ": " + leaf.getEnvelope());
		OutputStream os = new FileOutputStream(file);
		OsmOutputStream osmOutput = Util.setupOsmOutput(os, outputFormat,
				writeMetadata, pbfConfig);
		Output output = new Output(file, os, osmOutput);
		outputs.put(leaf, output);

		Envelope box = leaf.getEnvelope();
		osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box.getMaxY(),
				box.getMinY()));

		return output;
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

	protected abstract void handle(OsmNode node) throws IOException;

	@Override
	protected void finish() throws IOException
	{
		super.finish();

		for (Output output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

}

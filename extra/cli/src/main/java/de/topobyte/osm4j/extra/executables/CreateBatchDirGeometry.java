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

package de.topobyte.osm4j.extra.executables;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.batch.BatchFilesUtil;
import de.topobyte.osm4j.extra.datatree.BoxUtil;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateBatchDirGeometry extends AbstractExecutableInput
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_FILE_NAMES = "files";
	private static final String OPTION_OUTPUT = "output";

	private Path pathInput;
	private String fileNames;
	private Path pathOutput;

	@Override
	protected String getHelpMessage()
	{
		return CreateBatchDirGeometry.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateBatchDirGeometry task = new CreateBatchDirGeometry();

		task.setup(args);

		task.execute();
	}

	public CreateBatchDirGeometry()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT, true, true, "a directory containing batches");
		OptionHelper.addL(options, OPTION_FILE_NAMES, true, true, "file name in each batch to read");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "a wkt file to create");
		// @formatter:on
	}

	@Override
	public void setup(String[] args)
	{
		super.setup(args);

		String pathSpecInput = line.getOptionValue(OPTION_INPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
		String pathSpecOutput = line.getOptionValue(OPTION_OUTPUT);

		pathInput = Paths.get(pathSpecInput);
		pathOutput = Paths.get(pathSpecOutput);
	}

	private void execute() throws IOException
	{
		System.out.println("Opening directory: " + pathInput);

		GeometryFactory factory = new GeometryFactory();
		List<Geometry> boxList = new ArrayList<>();

		List<Path> paths = BatchFilesUtil.getPaths(pathInput, fileNames);

		for (Path path : paths) {
			InputStream input = new BufferedInputStream(
					Files.newInputStream(path));
			OsmIterator iterator = OsmIoUtils.setupOsmIterator(input,
					inputFormat, false);
			OsmBounds bounds = iterator.getBounds();
			input.close();

			Envelope e = new Envelope(bounds.getLeft(), bounds.getRight(),
					bounds.getBottom(), bounds.getTop());
			boxList.add(factory.toGeometry(e.intersection(BoxUtil.WORLD_BOUNDS)));
		}

		Geometry[] boxes = boxList.toArray(new Geometry[0]);

		GeometryCollection geometry = factory.createGeometryCollection(boxes);

		System.out.println("Writing output to: " + pathOutput);

		BufferedWriter writer = Files.newBufferedWriter(pathOutput,
				StandardCharsets.UTF_8);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

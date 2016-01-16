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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;

import de.topobyte.osm4j.extra.idbboxlist.IdBboxUtil;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateIdBboxListGeometry
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";

	private static final String HELP_MESSAGE = CreateIdBboxListGeometry.class
			.getSimpleName() + " [options]";

	public static void main(String[] args) throws IOException
	{
		Options options = new Options();

		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "an IdBboxList input file");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "a wkt file to create");
		// @formatter:on

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			System.out.println("unable to parse command line: "
					+ e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		String pathInput = line.getOptionValue(OPTION_INPUT);
		String pathOutput = line.getOptionValue(OPTION_OUTPUT);

		File dirTree = new File(pathInput);
		File fileOutput = new File(pathOutput);

		CreateIdBboxListGeometry task = new CreateIdBboxListGeometry(dirTree,
				fileOutput);
		task.execute();
	}

	private File fileInput;
	private File fileOutput;

	public CreateIdBboxListGeometry(File fileInput, File fileOutput)
	{
		this.fileInput = fileInput;
		this.fileOutput = fileOutput;
	}

	private void execute() throws IOException
	{
		System.out.println("Opening file: " + fileInput);

		List<Geometry> boxList = IdBboxUtil.readBoxes(fileInput);

		GeometryCollection geometry = new GeometryFactory()
				.createGeometryCollection(boxList.toArray(new Geometry[0]));

		System.out.println("Writing output to: " + fileOutput);

		FileWriter writer = new FileWriter(fileOutput);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

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
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.WKTWriter;

import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeBoxGeometry
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FORMAT = "format";

	private static final String HELP_MESSAGE = CreateNodeTreeBoxGeometry.class
			.getSimpleName() + " [options]";

	public static void main(String[] args) throws IOException
	{
		Options options = new Options();

		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "directory with node tree");
		OptionHelper.add(options, OPTION_FORMAT, true, true, "the file format of the data files");
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

		String formatName = line.getOptionValue(OPTION_FORMAT);
		FileFormat format = FileFormat.parseFileFormat(formatName);
		if (format == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		File dirTree = new File(pathInput);
		File fileOutput = new File(pathOutput);

		CreateNodeTreeBoxGeometry task = new CreateNodeTreeBoxGeometry(dirTree,
				format, fileOutput);
		task.execute();
	}

	private File dirTree;
	private FileFormat format;
	private File fileOutput;

	public CreateNodeTreeBoxGeometry(File dirTree, FileFormat format,
			File fileOutput)
	{
		this.dirTree = dirTree;
		this.format = format;
		this.fileOutput = fileOutput;
	}

	private void execute() throws IOException
	{
		System.out.println("Opening node tree: " + dirTree);

		DataTree tree = DataTreeOpener.open(dirTree, format);
		GeometryCollection geometry = BoxUtil.createBoxesGeometry(tree,
				BoxUtil.WORLD_BOUNDS);

		System.out.println("Writing output to: " + fileOutput);

		FileWriter writer = new FileWriter(fileOutput);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

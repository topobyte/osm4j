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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.topobyte.osm4j.extra.idbboxlist.IdBboxListGeometryCreator;
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
		OptionHelper.addL(options, OPTION_INPUT, true, true, "an IdBboxList input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "a wkt file to create");
		// @formatter:on

		CommandLine line = null;
		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.out
					.println("unable to parse command line: " + e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		String pathInput = line.getOptionValue(OPTION_INPUT);
		String pathOutput = line.getOptionValue(OPTION_OUTPUT);

		Path dirTree = Paths.get(pathInput);
		Path fileOutput = Paths.get(pathOutput);

		IdBboxListGeometryCreator task = new IdBboxListGeometryCreator(dirTree,
				fileOutput);
		task.execute();
	}

}

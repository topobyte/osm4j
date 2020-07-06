// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.diskstorage.tasks.NodeDbPopulator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * Create a node database from a osm tbo file.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmPopulateNodeDb
{

	static final Logger logger = LoggerFactory.getLogger(OsmPopulateNodeDb.class);

	private static final String HELP_MESSAGE = OsmPopulateNodeDb.class
			.getSimpleName() + " [args]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT_INDEX = "output-index";
	private static final String OPTION_OUTPUT_DATA = "output-data";

	/**
	 * @param args
	 *            input, output-index, output-data
	 */
	public static void main(String[] args)
	{
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, true, "an osm file");
		OptionHelper.addL(options, OPTION_OUTPUT_INDEX, true, true,
				"a node database to populate");
		OptionHelper.addL(options, OPTION_OUTPUT_DATA, true, true,
				"a node database to populate");

		CommandLine line = null;
		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			System.out
					.println("unable to parse command line: " + e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		if (line == null) {
			return;
		}

		Path input = Paths.get(line.getOptionValue(OPTION_INPUT));
		Path outputIndex = Paths.get(line.getOptionValue(OPTION_OUTPUT_INDEX));
		Path outputData = Paths.get(line.getOptionValue(OPTION_OUTPUT_DATA));

		NodeDbPopulator populator = new NodeDbPopulator(input, outputIndex,
				outputData);
		try {
			populator.execute();
		} catch (IOException e) {
			logger.error("Error while creating database: " + e.getMessage());
			System.exit(1);
		}

		System.exit(0);
	}

}

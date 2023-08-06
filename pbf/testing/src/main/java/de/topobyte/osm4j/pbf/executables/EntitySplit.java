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

package de.topobyte.osm4j.pbf.executables;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.pbf.seq.PbfEntitySplit;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class EntitySplit
{

	final static Logger logger = LoggerFactory.getLogger(EntitySplit.class);

	private static String HELP_MESSAGE = EntitySplit.class.getSimpleName()
			+ " [options]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT_NODES = "output_nodes";
	private static final String OPTION_OUTPUT_WAYS = "output_ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output_relations";

	public static void main(String[] args) throws IOException
	{
		// @formatter:off
		Options options = new Options();
		OptionHelper.addL(options, OPTION_INPUT, true, false, "input file");
		OptionHelper.addL(options, OPTION_OUTPUT_NODES, true, false, "the file to write nodes to");
		OptionHelper.addL(options, OPTION_OUTPUT_WAYS, true, false, "the file to write ways to");
		OptionHelper.addL(options, OPTION_OUTPUT_RELATIONS, true, false, "the file to write relations to");
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

		if (line == null) {
			return;
		}

		InputStream in = null;
		if (line.hasOption(OPTION_INPUT)) {
			String inputPath = line.getOptionValue(OPTION_INPUT);
			File file = new File(inputPath);
			FileInputStream fis = new FileInputStream(file);
			in = new BufferedInputStream(fis);
		} else {
			in = new BufferedInputStream(System.in);
		}

		OutputStream outNodes = null, outWays = null, outRelations = null;
		if (line.hasOption(OPTION_OUTPUT_NODES)) {
			String path = line.getOptionValue(OPTION_OUTPUT_NODES);
			FileOutputStream fos = new FileOutputStream(path);
			outNodes = new BufferedOutputStream(fos);
		}
		if (line.hasOption(OPTION_OUTPUT_WAYS)) {
			String path = line.getOptionValue(OPTION_OUTPUT_WAYS);
			FileOutputStream fos = new FileOutputStream(path);
			outWays = new BufferedOutputStream(fos);
		}
		if (line.hasOption(OPTION_OUTPUT_RELATIONS)) {
			String path = line.getOptionValue(OPTION_OUTPUT_RELATIONS);
			FileOutputStream fos = new FileOutputStream(path);
			outRelations = new BufferedOutputStream(fos);
		}

		if (outNodes == null && outWays == null && outRelations == null) {
			System.out.println(
					"You should specify an output for at least one entity");
			System.exit(1);
		}

		PbfEntitySplit task = new PbfEntitySplit(in, outNodes, outWays,
				outRelations);
		task.execute();
	}

}

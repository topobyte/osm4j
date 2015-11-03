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

package de.topobyte.osm4j.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractTaskSingleInputFile
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_INPUT_FORMAT = "input_format";

	protected abstract String getHelpMessage();

	protected Options options = new Options();
	protected CommandLine line = null;

	protected FileFormat inputFormat;
	protected String pathInput;

	public AbstractTaskSingleInputFile()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "the input file");
		OptionHelper.add(options, OPTION_INPUT_FORMAT, true, true, "the file format of the input");
		// @formatter:on
	}

	protected void setup(String[] args)
	{
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			System.out.println("unable to parse command line: "
					+ e.getMessage());
			new HelpFormatter().printHelp(getHelpMessage(), options);
			System.exit(1);
		}

		if (line == null) {
			return;
		}

		String inputFormatName = line.getOptionValue(OPTION_INPUT_FORMAT);
		inputFormat = FileFormat.parseFileFormat(inputFormatName);
		if (inputFormat == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pathInput = line.getOptionValue(OPTION_INPUT);
	}

	protected String getInputPath() throws IOException
	{
		return pathInput;
	}

	protected File getInputFile() throws IOException
	{
		return new File(pathInput);
	}

}

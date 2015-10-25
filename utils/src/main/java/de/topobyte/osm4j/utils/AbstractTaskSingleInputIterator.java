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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.pbf.access.PbfIterator;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractTaskSingleInputIterator
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_INPUT_FORMAT = "input_format";

	protected abstract String getHelpMessage();

	protected Options options = new Options();
	protected CommandLine line = null;

	protected FileFormat inputFormat;
	protected String pathInput = null;

	protected boolean readMetadata = true;

	protected boolean closeInput = true;
	protected InputStream in;
	protected OsmIterator inputIterator;

	public AbstractTaskSingleInputIterator()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, false, "the input file");
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

	protected void init() throws FileNotFoundException
	{
		in = null;
		if (pathInput == null) {
			closeInput = false;
			in = new BufferedInputStream(System.in);
		} else {
			closeInput = true;
			File file = new File(pathInput);
			FileInputStream fis = new FileInputStream(file);
			in = new BufferedInputStream(fis);
		}

		switch (inputFormat) {
		case XML:
			try {
				inputIterator = new OsmXmlIterator(in, readMetadata);
			} catch (ParserConfigurationException e) {
				System.out.println("unable to"
						+ " create xml reader (ParserConfigurationException): "
						+ e.getMessage());
			} catch (SAXException e) {
				System.out.println("unable to"
						+ " create xml reader (SAXException): "
						+ e.getMessage());
			}
			break;
		case TBO:
			inputIterator = new TboIterator(in);
			break;
		case PBF:
			inputIterator = new PbfIterator(in, readMetadata);
			break;
		}
	}

	protected void finish() throws IOException
	{
		if (closeInput) {
			in.close();
		}
	}

}

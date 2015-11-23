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

import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractExecutableInput extends AbstractExecutable
{

	private static final String OPTION_INPUT_FORMAT = "input_format";

	protected FileFormat inputFormat;
	protected boolean readTags = true;
	protected boolean readMetadata = true;

	public AbstractExecutableInput()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT_FORMAT, true, true, "the file format of the input");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String inputFormatName = line.getOptionValue(OPTION_INPUT_FORMAT);
		inputFormat = FileFormat.parseFileFormat(inputFormatName);
		if (inputFormat == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}
	}

}

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

package de.topobyte.osm4j.extra.datatree.nodetree;

import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class CreateNodeTreeBase extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES = "filenames";

	protected String pathOutput;
	protected String fileNames;

	public CreateNodeTreeBase()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
	}

}

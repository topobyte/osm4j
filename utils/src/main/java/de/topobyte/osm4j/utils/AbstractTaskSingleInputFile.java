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

import java.nio.file.Paths;

import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractTaskSingleInputFile extends AbstractTaskInput
{

	private static final String OPTION_INPUT = "input";

	protected String pathInput;

	public AbstractTaskSingleInputFile()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "the input file");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInput = line.getOptionValue(OPTION_INPUT);
	}

	protected OsmFile getOsmFile()
	{
		return new OsmFile(Paths.get(pathInput), inputFormat);
	}

	protected OsmFileInput getOsmFileInput()
	{
		return new OsmFileInput(getOsmFile());
	}

}

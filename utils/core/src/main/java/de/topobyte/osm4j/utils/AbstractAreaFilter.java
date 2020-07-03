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

public abstract class AbstractAreaFilter
		extends AbstractExecutableSingleInputStreamSingleOutput
{

	private static final String OPTION_ONLY_NODES = "nodes-only";

	protected boolean onlyNodes;

	public AbstractAreaFilter()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_ONLY_NODES, false, false, "extract only nodes");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		onlyNodes = false;
		onlyNodes = line.hasOption(OPTION_ONLY_NODES);
	}

}

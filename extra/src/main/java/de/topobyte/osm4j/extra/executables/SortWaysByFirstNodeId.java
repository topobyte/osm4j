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
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.ways.WaysSorterByFirstNodeId;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SortWaysByFirstNodeId extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return SortWaysByFirstNodeId.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SortWaysByFirstNodeId task = new SortWaysByFirstNodeId();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	protected String pathOutput;

	public SortWaysByFirstNodeId()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
	}

	private void execute() throws IOException
	{
		OsmIterator iterator = createIterator();

		WaysSorterByFirstNodeId sorter = new WaysSorterByFirstNodeId(iterator,
				Paths.get(pathOutput), outputFormat, pbfConfig, tboConfig,
				writeMetadata);

		sorter.execute();
	}

}

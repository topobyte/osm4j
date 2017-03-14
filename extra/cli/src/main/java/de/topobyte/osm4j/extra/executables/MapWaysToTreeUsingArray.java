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
import de.topobyte.osm4j.extra.datatree.ways.WaysToTreeMapperUsingArray;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class MapWaysToTreeUsingArray extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_FILE_NAMES = "filenames";
	private static final String OPTION_TREE = "tree";
	private static final String OPTION_NODE_ARRAY = "node-array";

	@Override
	protected String getHelpMessage()
	{
		return MapWaysToTreeUsingArray.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		MapWaysToTreeUsingArray task = new MapWaysToTreeUsingArray();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathTree;
	private String pathNodeArray;

	private String fileNames;

	public MapWaysToTreeUsingArray()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
		OptionHelper.addL(options, OPTION_TREE, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_NODE_ARRAY, true, true, "a path to a node array");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		fileNames = line.getOptionValue(OPTION_FILE_NAMES);

		pathTree = line.getOptionValue(OPTION_TREE);
		pathNodeArray = line.getOptionValue(OPTION_NODE_ARRAY);
	}

	public void execute() throws IOException
	{
		OsmIterator iterator = createIterator();
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		WaysToTreeMapperUsingArray mapper = new WaysToTreeMapperUsingArray(
				iterator, Paths.get(pathTree), fileNames,
				Paths.get(pathNodeArray), outputConfig);

		mapper.execute();
	}

}

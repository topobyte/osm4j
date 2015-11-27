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
import de.topobyte.osm4j.extra.datatree.ways.SimpleWaysToTreeMapper;
import de.topobyte.osm4j.extra.datatree.ways.WaysToTreeMapper;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class MapWaysToTree extends AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return MapWaysToTree.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		MapWaysToTree task = new MapWaysToTree();

		task.setup(args);

		task.readMetadata = false;
		task.readTags = false;

		task.init();

		task.execute();

		task.finish();
	}

	private String pathTree;
	private String pathWays;

	private String fileNamesOutput;

	public MapWaysToTree()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_WAYS, true, true, "directory with ways sorted by first node id");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathWays = line.getOptionValue(OPTION_WAYS);
		pathTree = line.getOptionValue(OPTION_TREE);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);
	}

	private void execute() throws IOException
	{
		OsmIterator iterator = createIterator();
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		WaysToTreeMapper mapper = new SimpleWaysToTreeMapper(iterator,
				Paths.get(pathTree), Paths.get(pathWays), inputFormat,
				fileNamesOutput, outputConfig);

		mapper.execute();
	}

}

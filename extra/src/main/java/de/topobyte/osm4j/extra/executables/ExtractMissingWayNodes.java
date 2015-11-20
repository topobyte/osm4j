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
import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesExtractor;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractMissingWayNodes extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_IDS = "ids";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return ExtractMissingWayNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractMissingWayNodes task = new ExtractMissingWayNodes();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathIdTree;
	private String pathOutputTree;

	private String fileNamesIds;
	private String fileNamesOutput;

	public ExtractMissingWayNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_FILE_NAMES_IDS, true, true, "names of the node id files in the tree");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String pathTree = line.getOptionValue(OPTION_TREE);
		pathIdTree = pathTree;
		pathOutputTree = pathTree;

		fileNamesIds = line.getOptionValue(OPTION_FILE_NAMES_IDS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);
	}

	public void execute() throws IOException
	{
		OsmIterator iterator = createIterator();
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		MissingWayNodesExtractor extractor = new MissingWayNodesExtractor(
				iterator, Paths.get(pathIdTree), fileNamesIds,
				Paths.get(pathOutputTree), fileNamesOutput, outputConfig);

		extractor.execute();
	}

}

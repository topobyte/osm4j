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

import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreatorMaxNodes;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeMaxNodes extends
		AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES = "filenames";
	private static final String OPTION_MAX_NODES = "max_nodes";

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeMaxNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeMaxNodes task = new CreateNodeTreeMaxNodes();

		task.setup(args);

		task.execute();
	}

	private int maxNodes;
	private String pathOutput;
	private String fileNames;

	public CreateNodeTreeMaxNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
		OptionHelper.add(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argMaxNodes = line.getOptionValue(OPTION_MAX_NODES);

		maxNodes = Integer.parseInt(argMaxNodes);
		if (maxNodes < 1) {
			System.out.println("Please specify a max nodes >= 1");
			System.exit(1);
		}

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		NodeTreeCreatorMaxNodes creator = new NodeTreeCreatorMaxNodes(
				getOsmFileInput(), maxNodes, SPLIT_INITIAL, SPLIT_ITERATION,
				Paths.get(pathOutput), fileNames, outputConfig);

		creator.init();

		creator.buildTree();
	}

}

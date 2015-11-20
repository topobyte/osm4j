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

import de.topobyte.osm4j.extra.datatree.ways.MissingWayNodesFinder;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class FindMissingWayNodes extends AbstractExecutableInput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_NODES = "nodes";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return FindMissingWayNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		FindMissingWayNodes task = new FindMissingWayNodes();

		task.setup(args);

		task.execute();
	}

	private String pathNodeTree;
	private String pathWayTree;
	private String pathOutputTree;

	private String fileNamesNodes;
	private String fileNamesWays;
	private String fileNamesOutput;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	public FindMissingWayNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES, true, true, "names of the node files in the tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the way files in the tree");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		inputFormatNodes = inputFormat;
		inputFormatWays = inputFormat;

		fileNamesNodes = line.getOptionValue(OPTION_FILE_NAMES_NODES);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);

		String pathTree = line.getOptionValue(OPTION_TREE);
		pathNodeTree = pathTree;
		pathWayTree = pathTree;
		pathOutputTree = pathTree;
	}

	public void execute() throws IOException
	{
		MissingWayNodesFinder finder = new MissingWayNodesFinder(
				Paths.get(pathNodeTree), Paths.get(pathWayTree),
				Paths.get(pathOutputTree), fileNamesNodes, fileNamesWays,
				fileNamesOutput, inputFormatNodes, inputFormatWays);

		finder.execute();
	}

}

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

import de.topobyte.osm4j.extra.datatree.ways.SimpleWaysDistributor;
import de.topobyte.osm4j.extra.datatree.ways.ThreadedWaysDistributor;
import de.topobyte.osm4j.extra.datatree.ways.WaysDistributor;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class DistributeWays extends AbstractExecutableInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_NODES1 = "nodes1";
	private static final String OPTION_FILE_NAMES_NODES2 = "nodes2";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT_WAYS = "ways-out";
	private static final String OPTION_FILE_NAMES_OUTPUT_NODES = "nodes-out";

	@Override
	protected String getHelpMessage()
	{
		return DistributeWays.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		DistributeWays task = new DistributeWays();

		task.setup(args);

		task.execute();
	}

	private String pathTree;

	private String fileNamesNodes1;
	private String fileNamesNodes2;
	private String fileNamesWays;
	private String fileNamesOutputWays;
	private String fileNamesOutputNodes;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	public DistributeWays()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_FILE_NAMES_NODES1, true, true, "names of the node files in the tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_NODES2, true, true, "names of the node files in the tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the way files in the tree");
		OptionHelper.addL(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.addL(options, OPTION_FILE_NAMES_OUTPUT_WAYS, true, true, "name of files for intersecting ways");
		OptionHelper.addL(options, OPTION_FILE_NAMES_OUTPUT_NODES, true, true, "name of files for intersecting ways' nodes");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		inputFormatNodes = inputFormat;
		inputFormatWays = inputFormat;

		fileNamesNodes1 = line.getOptionValue(OPTION_FILE_NAMES_NODES1);
		fileNamesNodes2 = line.getOptionValue(OPTION_FILE_NAMES_NODES2);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesOutputWays = line
				.getOptionValue(OPTION_FILE_NAMES_OUTPUT_WAYS);
		fileNamesOutputNodes = line
				.getOptionValue(OPTION_FILE_NAMES_OUTPUT_NODES);

		pathTree = line.getOptionValue(OPTION_TREE);
	}

	public void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		boolean threaded = true;

		WaysDistributor distributor;
		if (!threaded) {
			distributor = new SimpleWaysDistributor(Paths.get(pathTree),
					fileNamesNodes1, fileNamesNodes2, fileNamesWays,
					fileNamesOutputWays, fileNamesOutputNodes,
					inputFormatNodes, inputFormatWays, outputConfig);
		} else {
			distributor = new ThreadedWaysDistributor(Paths.get(pathTree),
					fileNamesNodes1, fileNamesNodes2, fileNamesWays,
					fileNamesOutputWays, fileNamesOutputNodes,
					inputFormatNodes, inputFormatWays, outputConfig);
		}

		distributor.execute();
	}

}

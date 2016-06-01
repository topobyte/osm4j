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
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.extra.relations.WayMemberNodeIdsExtractor;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractWayMemberNodeIds extends AbstractExecutableInput
{

	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_NODE_IDS = "node_ids";

	@Override
	protected String getHelpMessage()
	{
		return ExtractWayMemberNodeIds.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractWayMemberNodeIds task = new ExtractWayMemberNodeIds();

		task.setup(args);

		task.execute();
	}

	private String[] pathsData;

	private String fileNamesWays;
	private String fileNamesNodeIds;

	public ExtractWayMemberNodeIds()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_DIRECTORY, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the way files in each directory");
		OptionHelper.addL(options, OPTION_FILE_NAMES_NODE_IDS, true, true, "names of the node id files in each directory");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathsData = line.getOptionValues(OPTION_DIRECTORY);

		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesNodeIds = line.getOptionValue(OPTION_FILE_NAMES_NODE_IDS);
	}

	private void execute() throws IOException
	{
		Path[] dirsData = new Path[pathsData.length];
		for (int i = 0; i < dirsData.length; i++) {
			dirsData[i] = Paths.get(pathsData[i]);
		}

		WayMemberNodeIdsExtractor extractor = new WayMemberNodeIdsExtractor(
				dirsData, fileNamesWays, fileNamesNodeIds, inputFormat);
		extractor.execute();
	}

}

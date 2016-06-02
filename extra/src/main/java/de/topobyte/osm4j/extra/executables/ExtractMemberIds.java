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

import de.topobyte.osm4j.extra.relations.MemberIdsExtractor;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractMemberIds extends AbstractExecutableInput
{

	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_FILE_NAMES_NODE_IDS = "node-ids";
	private static final String OPTION_FILE_NAMES_WAY_IDS = "way-ids";

	@Override
	protected String getHelpMessage()
	{
		return ExtractMemberIds.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractMemberIds task = new ExtractMemberIds();

		task.setup(args);

		task.execute();
	}

	private String[] pathsData;

	private String fileNamesRelations;
	private String fileNamesNodeIds;
	private String fileNamesWayIds;

	public ExtractMemberIds()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_DIRECTORY, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		OptionHelper.addL(options, OPTION_FILE_NAMES_NODE_IDS, true, true, "names of the node id files in each directory");
		OptionHelper.addL(options, OPTION_FILE_NAMES_WAY_IDS, true, true, "names of the way id files in each directory");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathsData = line.getOptionValues(OPTION_DIRECTORY);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
		fileNamesNodeIds = line.getOptionValue(OPTION_FILE_NAMES_NODE_IDS);
		fileNamesWayIds = line.getOptionValue(OPTION_FILE_NAMES_WAY_IDS);
	}

	private void execute() throws IOException
	{
		Path[] dirsData = new Path[pathsData.length];
		for (int i = 0; i < dirsData.length; i++) {
			dirsData[i] = Paths.get(pathsData[i]);
		}

		MemberIdsExtractor extractor = new MemberIdsExtractor(dirsData,
				fileNamesRelations, fileNamesNodeIds, fileNamesWayIds,
				inputFormat);
		extractor.execute();
	}

}

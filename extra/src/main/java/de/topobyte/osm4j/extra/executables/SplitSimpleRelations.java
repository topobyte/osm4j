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

import de.topobyte.osm4j.extra.relations.split.SimpleRelationSplitter;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmStreamInput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SplitSimpleRelations extends AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";

	@Override
	protected String getHelpMessage()
	{
		return SplitSimpleRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitSimpleRelations task = new SplitSimpleRelations();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathOutput;

	private String fileNamesRelations;

	public SplitSimpleRelations()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
	}

	private void execute() throws IOException
	{
		OsmStreamInput streamInput = new OsmStreamInput(osmStream);
		SimpleRelationSplitter splitter = new SimpleRelationSplitter(
				Paths.get(pathOutput), fileNamesRelations, streamInput,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		splitter.execute();
	}

}

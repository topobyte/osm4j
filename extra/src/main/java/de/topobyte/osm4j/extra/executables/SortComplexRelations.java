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

import de.topobyte.osm4j.extra.relations.split.ComplexRelationSorter;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SortComplexRelations extends
		AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_INPUT_BBOXES = "bboxes";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_OUTPUT_BBOXES = "output_bboxes";
	private static final String OPTION_MAX_MEMBERS = "max_members";

	@Override
	protected String getHelpMessage()
	{
		return SortComplexRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SortComplexRelations task = new SortComplexRelations();

		task.setup(args);

		task.execute();
	}

	private String pathInputBboxes;
	private String pathOutput;
	private String pathOutputBboxes;

	private String fileNamesRelations;

	private int maxMembers;

	public SortComplexRelations()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT_BBOXES, true, true, "bbox information file");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		OptionHelper.add(options, OPTION_OUTPUT_BBOXES, true, true, "bbox information file");
		OptionHelper.add(options, OPTION_MAX_MEMBERS, true, true, "maximum number of nodes per batch");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInputBboxes = line.getOptionValue(OPTION_INPUT_BBOXES);
		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		pathOutputBboxes = line.getOptionValue(OPTION_OUTPUT_BBOXES);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);

		try {
			maxMembers = ArgumentHelper.getInteger(line, OPTION_MAX_MEMBERS)
					.getValue();
		} catch (ArgumentParseException e) {
			System.out.println(String.format(
					"Error while parsing option '%s': %s", OPTION_MAX_MEMBERS,
					e.getMessage()));
			System.exit(1);
		}
	}

	private void execute() throws IOException
	{
		OsmFileInput fileInput = getOsmFileInput();

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		ComplexRelationSorter sorter = new ComplexRelationSorter(
				Paths.get(pathInputBboxes), Paths.get(pathOutput),
				fileNamesRelations, fileInput, outputConfig,
				Paths.get(pathOutputBboxes), maxMembers);

		sorter.execute();
	}

}

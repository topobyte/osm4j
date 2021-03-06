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

import de.topobyte.osm4j.extra.OutputUtil;
import de.topobyte.osm4j.extra.relations.split.ComplexRelationSplitter;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SplitComplexRelations
		extends AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";

	@Override
	protected String getHelpMessage()
	{
		return SplitComplexRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitComplexRelations task = new SplitComplexRelations();

		task.setup(args);

		task.init();

		task.execute();
	}

	private String pathOutput;

	private Path dirOutput;
	private String fileNamesRelations;

	public SplitComplexRelations()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
	}

	protected void init() throws IOException
	{
		dirOutput = Paths.get(pathOutput);
		OutputUtil.ensureOutputDirectory(dirOutput);
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		ComplexRelationSplitter splitter = new ComplexRelationSplitter(
				Paths.get(pathOutput), fileNamesRelations, getOsmFileInput(),
				outputConfig);

		splitter.execute();
	}

}

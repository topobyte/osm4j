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

import de.topobyte.osm4j.extra.relations.RelationsSeparator;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SeparateRelations extends AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT_SIMPLE = "output_simple";
	private static final String OPTION_OUTPUT_COMPLEX = "output_complex";

	@Override
	protected String getHelpMessage()
	{
		return SeparateRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SeparateRelations task = new SeparateRelations();

		task.setup(args);

		task.execute();
	}

	private String pathOutputSimple;
	private String pathOutputComplex;

	public SeparateRelations()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT_SIMPLE, true, true, "output: simple relations");
		OptionHelper.addL(options, OPTION_OUTPUT_COMPLEX, true, true, "output: complex relations");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutputSimple = line.getOptionValue(OPTION_OUTPUT_SIMPLE);
		pathOutputComplex = line.getOptionValue(OPTION_OUTPUT_COMPLEX);
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		RelationsSeparator separator = new RelationsSeparator(
				getOsmFileInput(), Paths.get(pathOutputSimple),
				Paths.get(pathOutputComplex), outputConfig);

		separator.execute();
	}

}

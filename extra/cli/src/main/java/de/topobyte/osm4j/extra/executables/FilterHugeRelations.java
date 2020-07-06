// Copyright 2020 Sebastian Kuerten
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

import de.topobyte.osm4j.extra.relations.HugeRelationsFilter;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class FilterHugeRelations extends AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT_HUGE = "output-huge";
	private static final String OPTION_OUTPUT_REMAINDER = "output-remainder";
	private static final String OPTION_MAX_MEMBERS = "max-members";

	@Override
	protected String getHelpMessage()
	{
		return FilterHugeRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		FilterHugeRelations task = new FilterHugeRelations();

		task.setup(args);

		task.execute();
	}

	private String pathOutputHuge;
	private String pathOutputRemainder;
	private int maxMembers;

	public FilterHugeRelations()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT_HUGE, true, true, "output: huge relations");
		OptionHelper.addL(options, OPTION_OUTPUT_REMAINDER, true, true, "output: remaining relations");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS, true, true, "maximum members for a relation to be considered non-huge");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutputHuge = line.getOptionValue(OPTION_OUTPUT_HUGE);
		pathOutputRemainder = line.getOptionValue(OPTION_OUTPUT_REMAINDER);
		String argMaxMembers = line.getOptionValue(OPTION_MAX_MEMBERS);
		maxMembers = Integer.parseInt(argMaxMembers);
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		HugeRelationsFilter filter = new HugeRelationsFilter(getOsmFileInput(),
				Paths.get(pathOutputHuge), Paths.get(pathOutputRemainder),
				maxMembers, outputConfig);

		filter.execute();
	}

}

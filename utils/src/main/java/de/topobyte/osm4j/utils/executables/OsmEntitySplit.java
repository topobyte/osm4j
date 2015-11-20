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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.split.EntitySplitter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmEntitySplit extends AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT_NODES = "output_nodes";
	private static final String OPTION_OUTPUT_WAYS = "output_ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output_relations";

	@Override
	protected String getHelpMessage()
	{
		return OsmEntitySplit.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmEntitySplit task = new OsmEntitySplit();
		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathNodes = null;
	private String pathWays = null;
	private String pathRelations = null;

	public OsmEntitySplit()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT_NODES, true, false, "the file to write nodes to");
		OptionHelper.add(options, OPTION_OUTPUT_WAYS, true, false, "the file to write ways to");
		OptionHelper.add(options, OPTION_OUTPUT_RELATIONS, true, false, "the file to write relations to");
		// @formatter:on
	}

	@Override
	public void setup(String[] args)
	{
		super.setup(args);

		if (line.hasOption(OPTION_OUTPUT_NODES)) {
			pathNodes = line.getOptionValue(OPTION_OUTPUT_NODES);
		}
		if (line.hasOption(OPTION_OUTPUT_WAYS)) {
			pathWays = line.getOptionValue(OPTION_OUTPUT_WAYS);
		}
		if (line.hasOption(OPTION_OUTPUT_RELATIONS)) {
			pathRelations = line.getOptionValue(OPTION_OUTPUT_RELATIONS);
		}

		if (pathNodes == null && pathWays == null && pathRelations == null) {
			System.out
					.println("You should specify an output for at least one entity");
			System.exit(1);
		}
	}

	public void execute() throws IOException
	{
		OsmIterator iterator = createIterator();

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		EntitySplitter splitter = new EntitySplitter(iterator,
				Paths.get(pathNodes), Paths.get(pathWays),
				Paths.get(pathRelations), outputConfig);

		splitter.execute();
	}

}

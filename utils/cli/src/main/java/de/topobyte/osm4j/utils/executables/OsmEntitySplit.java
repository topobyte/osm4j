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
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.split.EntitySplitter;
import de.topobyte.osm4j.utils.split.ThreadedEntitySplitter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmEntitySplit extends AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT_NODES = "output-nodes";
	private static final String OPTION_OUTPUT_WAYS = "output-ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output-relations";
	private static final String OPTION_THREADED = "threaded";

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

	private boolean useThreadedVersion;

	public OsmEntitySplit()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT_NODES, true, false, "the file to write nodes to");
		OptionHelper.addL(options, OPTION_OUTPUT_WAYS, true, false, "the file to write ways to");
		OptionHelper.addL(options, OPTION_OUTPUT_RELATIONS, true, false, "the file to write relations to");
		OptionHelper.addL(options, OPTION_THREADED, false, false, "use a multi-threaded implementation");
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

		useThreadedVersion = line.hasOption(OPTION_THREADED);
	}

	public void execute() throws IOException
	{
		OsmIterator iterator = createIterator();

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		if (!useThreadedVersion) {
			EntitySplitter splitter = new EntitySplitter(iterator,
					path(pathNodes), path(pathWays), path(pathRelations),
					outputConfig);
			splitter.execute();
		} else {
			ThreadedEntitySplitter splitter = new ThreadedEntitySplitter(
					iterator, path(pathNodes), path(pathWays),
					path(pathRelations), outputConfig, 10000, 200);
			splitter.execute();
		}
	}

	private Path path(String path)
	{
		if (path == null) {
			return null;
		}
		return Paths.get(path);
	}

}

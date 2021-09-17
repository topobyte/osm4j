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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.diskstorage.tasks.WayDbPopulator;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * Create a way database from an osm file.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmPopulateWayDb extends AbstractExecutableSingleInputStream
{

	static final Logger logger = LoggerFactory
			.getLogger(OsmPopulateWayDb.class);

	private static final String OPTION_OUTPUT_INDEX = "output-index";
	private static final String OPTION_OUTPUT_DATA = "output-data";
	private static final String OPTION_USE_TAGS = "use-tags";

	@Override
	protected String getHelpMessage()
	{
		return OsmPopulateWayDb.class.getSimpleName() + " [options]";
	}

	/**
	 * @param args
	 *            input, output-index, output-data
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		OsmPopulateWayDb task = new OsmPopulateWayDb();
		task.setup(args);

		task.init();

		try {
			task.run();
		} catch (IOException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	private Path outputIndex;
	private Path outputData;
	private boolean useTags;

	public OsmPopulateWayDb()
	{
		OptionHelper.addL(options, OPTION_OUTPUT_INDEX, true, true,
				"a way database to populate");
		OptionHelper.addL(options, OPTION_OUTPUT_DATA, true, true,
				"a way database to populate");
		OptionHelper.addL(options, OPTION_USE_TAGS, true, false,
				"whether to use tags <true|false>, default: true");
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		outputIndex = Paths.get(line.getOptionValue(OPTION_OUTPUT_INDEX));
		outputData = Paths.get(line.getOptionValue(OPTION_OUTPUT_DATA));
		String useTagsArgument = line.getOptionValue(OPTION_USE_TAGS);
		useTags = true;
		if (line.hasOption(OPTION_USE_TAGS)) {
			useTags = !useTagsArgument.equals("false");
		}
	}

	@Override
	protected void init() throws IOException
	{
		super.init();
		readMetadata = false;
		readTags = useTags;
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		WayDbPopulator populator = new WayDbPopulator(iterator, outputIndex,
				outputData, useTags);
		populator.execute();
	}

}

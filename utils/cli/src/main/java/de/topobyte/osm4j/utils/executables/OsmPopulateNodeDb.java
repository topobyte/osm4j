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
import de.topobyte.osm4j.diskstorage.tasks.NodeDbPopulator;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

/**
 * Create a node database from an osm file.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmPopulateNodeDb extends AbstractExecutableSingleInputStream
{

	static final Logger logger = LoggerFactory
			.getLogger(OsmPopulateNodeDb.class);

	private static final String OPTION_OUTPUT_INDEX = "output-index";
	private static final String OPTION_OUTPUT_DATA = "output-data";

	@Override
	protected String getHelpMessage()
	{
		return OsmPopulateNodeDb.class.getSimpleName() + " [options]";
	}

	/**
	 * @param args
	 *            input, output-index, output-data
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		OsmPopulateNodeDb task = new OsmPopulateNodeDb();
		task.setup(args);

		task.readMetadata = false;
		task.readTags = false;
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

	public OsmPopulateNodeDb()
	{
		OptionHelper.addL(options, OPTION_OUTPUT_INDEX, true, true,
				"a node database to populate");
		OptionHelper.addL(options, OPTION_OUTPUT_DATA, true, true,
				"a node database to populate");
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		outputIndex = Paths.get(line.getOptionValue(OPTION_OUTPUT_INDEX));
		outputData = Paths.get(line.getOptionValue(OPTION_OUTPUT_DATA));
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		NodeDbPopulator populator = new NodeDbPopulator(iterator, outputIndex,
				outputData);
		populator.execute();
	}

}

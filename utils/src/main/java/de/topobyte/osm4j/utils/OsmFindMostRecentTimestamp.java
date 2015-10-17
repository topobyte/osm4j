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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmFindMostRecentTimestamp extends AbstractTaskSingleInputIterator
{

	private static final String OPTION_VERBOSE = "verbose";

	@Override
	protected String getHelpMessage()
	{
		return OsmFindMostRecentTimestamp.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmFindMostRecentTimestamp task = new OsmFindMostRecentTimestamp();
		task.setup(args);

		if (task.inputFormat == FileFormat.TBO) {
			System.out.println("Tbo file format does not support metadata");
			System.exit(1);
		}

		task.readMetadata = true;
		task.init();

		task.run();

		task.finish();
	}

	public OsmFindMostRecentTimestamp()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_VERBOSE, false, false, "report each more recent timestamp as soon as it is found");
		// @formatter:on
	}

	private boolean verbose;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		verbose = line.hasOption(OPTION_VERBOSE);
	}

	private void run() throws IOException
	{
		long latest = 0;

		while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
			OsmEntity entity = entityContainer.getEntity();

			OsmMetadata metadata = entity.getMetadata();
			long time = metadata.getTimestamp();
			if (time > latest) {
				latest = time;
				if (verbose) {
					printTime(latest);
				}
			}
		}

		printTime(latest);

		finish();
	}

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private void printTime(long time)
	{
		System.out.println(dateFormat.format(time));
	}

}

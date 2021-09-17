// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.melon.io.ModTimes;
import de.topobyte.melon.paths.PathUtil;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.diskstorage.tasks.NodeDbPopulator;
import de.topobyte.osm4j.diskstorage.tasks.WayDbPopulator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;

public class EntityDbSetup
{

	final static Logger logger = LoggerFactory.getLogger(EntityDbSetup.class);

	public static void createNodeDb(Path inputFile, Path indexFile,
			Path dataFile) throws IOException
	{
		createNodeDb(inputFile, indexFile, dataFile, false);
	}

	public static void createNodeDb(Path inputFile, Path indexFile,
			Path dataFile, boolean force) throws IOException
	{
		PathUtil.createParentDirectories(dataFile);

		if (!force && ModTimes.isNewerThan(indexFile, inputFile)) {
			logger.info("node index is newer than input, skipping creation");
		} else {
			OsmFileInput input = new OsmFileInput(inputFile, FileFormat.TBO);
			OsmIteratorInput iterator = input.createIterator(false, false);
			NodeDbPopulator nodeDbPopulator = new NodeDbPopulator(
					iterator.getIterator(), indexFile, dataFile);
			nodeDbPopulator.execute();
		}
	}

	public static void createWayDb(Path inputFile, Path indexFile,
			Path dataFile, boolean useTags) throws IOException
	{
		createWayDb(inputFile, indexFile, dataFile, useTags, false);
	}

	public static void createWayDb(Path inputFile, Path indexFile,
			Path dataFile, boolean useTags, boolean force) throws IOException
	{
		PathUtil.createParentDirectories(dataFile);

		if (!force && ModTimes.isNewerThan(indexFile, inputFile)) {
			logger.info("way index is newer than input, skipping creation");
		} else {
			OsmFileInput input = new OsmFileInput(inputFile, FileFormat.TBO);
			OsmIteratorInput iterator = input.createIterator(useTags, false);
			WayDbPopulator wayDbPopulator = new WayDbPopulator(
					iterator.getIterator(), indexFile, dataFile, useTags);
			wayDbPopulator.execute();
		}
	}

}

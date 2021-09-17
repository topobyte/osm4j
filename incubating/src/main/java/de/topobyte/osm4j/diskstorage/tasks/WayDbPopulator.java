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

package de.topobyte.osm4j.diskstorage.tasks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecord;
import de.topobyte.osm4j.diskstorage.waydb.WayRecordWithTags;

/**
 * Create a way database from a osm file.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayDbPopulator
{

	static final Logger logger = LoggerFactory.getLogger(WayDbPopulator.class);

	private final OsmIterator iterator;
	private final Path outputIndex;
	private final Path outputData;
	private final boolean useTags;

	public WayDbPopulator(OsmIterator iterator, Path outputIndex,
			Path outputData, boolean useTags)
	{
		this.iterator = iterator;
		this.outputIndex = outputIndex;
		this.outputData = outputData;
		this.useTags = useTags;
	}

	public void execute() throws IOException
	{
		// make sure we have fresh files for the way database.
		logger.debug("making sure database is empty");
		if (Files.exists(outputIndex)) {
			Files.delete(outputIndex);
			if (Files.exists(outputIndex)) {
				throw new IOException("unable to delete existing index");
			}
		}
		if (Files.exists(outputData)) {
			Files.delete(outputData);
			if (Files.exists(outputData)) {
				throw new IOException("unable to delete existing database");
			}
		}

		// create the database
		logger.debug("creating database");
		VarDB<?> wayDB;
		try {
			if (useTags) {
				wayDB = new VarDB<>(outputData, outputIndex,
						new WayRecordWithTags(0));
			} else {
				wayDB = new VarDB<>(outputData, outputIndex, new WayRecord(0));
			}
		} catch (FileNotFoundException e) {
			logger.error("unable to create database");
			throw new IOException("unable to create database");
		}

		// read and insert ways
		logger.debug("inserting ways");
		int i = 0; // count ways
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Way) {
				Way way = (Way) container.getEntity();

				TLongList nodeIds = new TLongArrayList();
				for (long nodeId : way.getNodes().toArray()) {
					nodeIds.add(nodeId);
				}
				if (useTags) {
					Map<String, String> tags = new HashMap<>();
					for (OsmTag tag : way.getTags()) {
						tags.put(tag.getKey(), tag.getValue());
					}
					wayDB.addRecord(
							new WayRecordWithTags(way.getId(), nodeIds, tags));
				} else {
					wayDB.addRecord(new WayRecord(way.getId(), nodeIds));
				}
				i++;
				if ((i % 10000) == 0) {
					logger.debug("ways inserted: " + i);
				}
			}
		}
		logger.debug("ways inserted: " + i);

		// close database
		logger.debug("closing database");
		try {
			wayDB.close();
		} catch (IOException e) {
			throw new IOException("unable to close database");
		}
	}

}

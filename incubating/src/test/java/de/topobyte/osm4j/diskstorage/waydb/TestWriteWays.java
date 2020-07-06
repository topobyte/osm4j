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

package de.topobyte.osm4j.diskstorage.waydb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.list.TLongList;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.tbo.access.TboIterator;

/**
 * Test class for ways with vardb.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestWriteWays
{

	final static Logger logger = LoggerFactory.getLogger(TestWriteWays.class);

	/**
	 * Add all input ways to the database.
	 * 
	 * @param args
	 *            currently none.
	 * @throws IOException
	 *             on io failure.
	 */
	public static void main(String[] args) throws IOException
	{
		Path fileDB = Paths.get("/tmp/vardb.ways.withtags.dat");
		Path fileIndex = Paths.get("/tmp/vardb.ways.withtags.idx");
		Path fileData = Paths.get("/tmp/highways.ways.tbo");

		VarDB<WayRecordWithTags> varDB = new VarDB<>(fileDB, fileIndex,
				new WayRecordWithTags(0));

		// int i = 0;
		TboIterator iterator = new TboIterator(Files.newInputStream(fileData),
				true, false);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Way) {
				OsmWay way = (OsmWay) container.getEntity();
				// if (i++ > 2){
				// break;
				// }

				TLongList nodeIds = OsmModelUtil.nodesAsList(way);
				Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

				WayRecordWithTags wayRecord = new WayRecordWithTags(way.getId(),
						nodeIds, tags);
				varDB.addRecord(wayRecord);
				logger.debug("added way with id: " + way.getId());
			}
		}

		varDB.close();
		System.exit(0);
	}

}

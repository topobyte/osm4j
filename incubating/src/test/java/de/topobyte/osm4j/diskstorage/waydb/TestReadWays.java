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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.collections.util.ListUtil;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.diskstorage.Cache;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.tbo.access.TboIterator;

/**
 * Test class for ways with vardb.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestReadWays
{

	final static Logger logger = LoggerFactory.getLogger(TestReadWays.class);

	/**
	 * Perfom a find operation on the database for each way in the input data.
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

		// search(varDB, 2215317);
		// search(varDB, 2215367);
		// search(varDB, 2215447);
		// search(varDB, 2233155);

		List<Long> ids = new ArrayList<>();

		// int i = 0;
		TboIterator iterator = new TboIterator(Files.newInputStream(fileData),
				true, false);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Way) {
				OsmWay way = (OsmWay) container.getEntity();
				// if (i++ > 14000){
				// break;
				// }
				// logger.debug("test number " + i++);
				ids.add(way.getId());
				// search(varDB, way.getId());
				// searchAndCompare(way, varDB, way.getId());
			}
		}

		ListUtil.shuffle(ids, ids.size() * 10);

		for (long id : ids) {
			search(varDB, id);
		}

		varDB.close();

		Cache.printStatistics();

		System.exit(0);
	}

	private static void search(VarDB<WayRecordWithTags> varDB, long id)
			throws IOException
	{
		logger.debug("searching for: " + id);
		WayRecordWithTags find = varDB.find(id);
		if (find == null) {
			logger.debug("not found");
			return;
		}
		logger.debug("found: " + find + " number of nodes: "
				+ find.getNodeIds().size());
		String name = find.getTags().get("name");
		if (name != null) {
			logger.debug(name);
		}
	}

	// private static void searchAndCompare(Way way, VarDB<WayRecord> varDB,
	// long id) throws IOException{
	// logger.debug("searching for: " + id);
	// WayRecord find = varDB.find(id);
	// if (find == null){
	// logger.debug("not found");
	// }else{
	// logger.debug("found: " + find + " number of nodes: " +
	// find.getNodeIds().size());
	//
	// if (way.getWayNodes().size() != find.getNodeIds().size()){
	// logger.error("number of nodes different");
	// }
	// for (int i = 0; i < way.getWayNodes().size(); i++){
	// WayNode wayNode = way.getWayNodes().get(i);
	// long nodeId = find.getNodeIds().get(i);
	// if (wayNode.getNodeId() != nodeId){
	// logger.error("a node id is different");
	// }
	// }
	// }
	// }

}

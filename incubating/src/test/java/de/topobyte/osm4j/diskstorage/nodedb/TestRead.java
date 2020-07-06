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

package de.topobyte.osm4j.diskstorage.nodedb;

import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.diskstorage.nodedb.Block;
import de.topobyte.osm4j.diskstorage.nodedb.DbNode;
import de.topobyte.osm4j.diskstorage.nodedb.Entry;
import de.topobyte.osm4j.diskstorage.nodedb.Index;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;

/**
 * Test class that reads a single node from database
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestRead
{

	/**
	 * @param args
	 *            none
	 * @throws IOException
	 *             on failure
	 */
	public static void main(String args[]) throws IOException
	{
		NodeDB nodeDB = new NodeDB(Paths.get("/tmp/raf.bin"),
				Paths.get("/tmp/raf.index"));

		Index index = nodeDB.getIndex();
		System.out.println(
				"number of blocks in file: " + index.getEntries().size());

		// for (int i = 0; i < index.entries.size(); i++){
		// Block b = nodeDB.getBlock(i * 4096);
		// System.out.println("block " + i + ": " + b);
		// }
		// System.exit(0);

		long id = 5070334;
		Entry find = index.find(id);
		if (find == null) {
			System.out.println("not found");
		} else {
			System.out.println("pos: " + find.getPosition());
			Block block = nodeDB.getBlock(find.getPosition());
			System.out.println(block);
			System.out.println("blocksize: " + block.getNodes().size());
			DbNode node = block.find(id);
			if (node == null) {
				System.out.println("node not found");
			} else {
				System.out.println(node.getId() + ": " + node.getLon() + ","
						+ node.getLat());
			}
		}

		nodeDB.close();
	}

}

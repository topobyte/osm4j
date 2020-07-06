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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.tbo.access.TboIterator;

/**
 * Test class that populates a database.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestFill
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

		int i = 0;

		TboIterator iterator = new TboIterator(
				new FileInputStream("/tmp/highways.tbo"), false, false);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				OsmNode node = (OsmNode) container.getEntity();

				DbNode n = new DbNode(node.getId(), node.getLongitude(),
						node.getLatitude());
				nodeDB.addNode(n);
				if ((i++ % 1000) == 0) {
					System.out.println("done " + i);
					// if (i > 40000)
					// break;
				}
			}
		}
		nodeDB.close();
		System.exit(0);
	}

}

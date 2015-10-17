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

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmCount extends AbstractTaskSingleInputIterator
{

	@Override
	protected String getHelpMessage()
	{
		return OsmCount.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmCount task = new OsmCount();
		task.setup(args);

		task.readMetadata = false;
		task.init();

		task.run();

		task.finish();
	}

	private long nc = 0, wc = 0, rc = 0;
	private long closedWays = 0;
	private long wayNodes = 0;
	private long relationMembers = 0;

	private void run() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
			switch (entityContainer.getType()) {
			case Node:
				nc++;
				break;
			case Way:
				wc++;
				OsmWay way = (OsmWay) entityContainer.getEntity();
				boolean closed = way.getNodeId(0) == way.getNodeId(way
						.getNumberOfNodes() - 1);
				if (closed) {
					closedWays++;
				}
				wayNodes += way.getNumberOfNodes();
				break;
			case Relation:
				rc++;
				OsmRelation relation = (OsmRelation) entityContainer
						.getEntity();
				relationMembers += relation.getNumberOfMembers();
				break;
			}
		}

		System.out.println("nodes:            " + nc);
		System.out.println("ways:             " + wc);
		System.out.println("ways (closed):    " + closedWays);
		System.out.println("waynodes:         " + wayNodes);
		System.out.println("relations:        " + rc);
		System.out.println("relation members: " + relationMembers);

		finish();
	}

}

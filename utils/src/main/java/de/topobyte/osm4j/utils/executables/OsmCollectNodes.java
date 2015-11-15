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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.AbstractEntityCollector;

public class OsmCollectNodes extends AbstractEntityCollector
{

	@Override
	protected String getHelpMessage()
	{
		return OsmCollectNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmCollectNodes task = new OsmCollectNodes();

		task.setup(args);

		task.init();

		try {
			task.run();
		} catch (OsmInputException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	@Override
	protected void readReferences()
	{
		while (iteratorReferences.hasNext()) {
			EntityContainer container = iteratorReferences.next();
			if (container.getType() == EntityType.Way) {
				OsmWay way = (OsmWay) container.getEntity();
				for (int i = 0; i < way.getNumberOfNodes(); i++) {
					ids.add(way.getNodeId(i));
				}
			} else if (container.getType() == EntityType.Relation) {
				OsmRelation relation = (OsmRelation) container.getEntity();
				for (int i = 0; i < relation.getNumberOfMembers(); i++) {
					OsmRelationMember member = relation.getMember(i);
					if (member.getType() == EntityType.Node) {
						ids.add(member.getId());
					}
				}
			}
		}
	}

	@Override
	protected boolean take(OsmNode node)
	{
		return ids.contains(node.getId());
	}

	@Override
	protected boolean take(OsmWay way)
	{
		return false;
	}

	@Override
	protected boolean take(OsmRelation relation)
	{
		return false;
	}

}

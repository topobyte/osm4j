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

import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIteratorSingleOutput;

public class OsmDropTags extends AbstractTaskSingleInputIteratorSingleOutput
{

	@Override
	protected String getHelpMessage()
	{
		return OsmDropTags.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmDropTags convert = new OsmDropTags();

		convert.setup(args);

		convert.readMetadata = true;
		convert.writeMetadata = true;

		convert.init();

		convert.run();

		convert.finish();
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
	}

	protected void run() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
			switch (entityContainer.getType()) {
			case Node: {
				OsmNode node = (OsmNode) entityContainer.getEntity();
				OsmNode copy = new Node(node.getId(), node.getLongitude(),
						node.getLatitude(), node.getMetadata());
				osmOutputStream.write(copy);
				break;
			}
			case Way: {
				OsmWay way = (OsmWay) entityContainer.getEntity();
				TLongArrayList nodes = new TLongArrayList();
				for (int i = 0; i < way.getNumberOfNodes(); i++) {
					nodes.add(way.getNodeId(i));
				}
				OsmWay copy = new Way(way.getId(), nodes, way.getMetadata());
				osmOutputStream.write(copy);
				break;
			}
			case Relation: {
				OsmRelation relation = (OsmRelation) entityContainer
						.getEntity();
				List<OsmRelationMember> members = new ArrayList<OsmRelationMember>();
				for (int i = 0; i < relation.getNumberOfMembers(); i++) {
					members.add(relation.getMember(i));
				}
				Relation copy = new Relation(relation.getId(), members,
						relation.getMetadata());
				osmOutputStream.write(copy);
				break;
			}
			}
		}
		osmOutputStream.complete();
	}

}

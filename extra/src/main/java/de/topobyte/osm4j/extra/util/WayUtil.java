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

package de.topobyte.osm4j.extra.util;

import java.util.Collection;
import java.util.Set;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class WayUtil
{

	public static void findNodes(OsmWay way, Set<OsmNode> nodes,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			nodes.add(entityProvider.getNode(way.getNodeId(i)));
		}
	}

	public static void findNodes(Collection<OsmWay> ways, Set<OsmNode> nodes,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		for (OsmWay w : ways) {
			findNodes(w, nodes, entityProvider);
		}
	}

}

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

package de.topobyte.osm4j.extra.relations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class RelationUtil
{

	public static Set<OsmNode> findNodes(OsmRelation relation,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		Set<OsmWay> ways = new HashSet<>();
		Set<OsmNode> nodes = new HashSet<>();

		findNodesAndWays(relation, ways, nodes, entityProvider);

		findNodes(ways, nodes, entityProvider);

		return nodes;
	}

	public static Set<OsmNode> findNodes(Collection<OsmRelation> relations,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		Set<OsmWay> ways = new HashSet<>();
		Set<OsmNode> nodes = new HashSet<>();

		for (OsmRelation r : relations) {
			findNodesAndWays(r, ways, nodes, entityProvider);
		}

		findNodes(ways, nodes, entityProvider);

		return nodes;
	}

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

	public static void findNodesAndWays(OsmRelation relation, Set<OsmWay> ways,
			Set<OsmNode> nodes, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			if (member.getType() == EntityType.Way) {
				ways.add(entityProvider.getWay(member.getId()));
			} else if (member.getType() == EntityType.Node) {
				nodes.add(entityProvider.getNode(member.getId()));
			}
		}
	}

}

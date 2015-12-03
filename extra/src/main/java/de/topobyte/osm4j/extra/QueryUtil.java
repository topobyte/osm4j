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

package de.topobyte.osm4j.extra;

import gnu.trove.map.TLongObjectMap;

import java.io.IOException;
import java.util.Arrays;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class QueryUtil
{

	public static void putNodes(OsmWay way, TLongObjectMap<OsmNode> nodes,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			nodes.put(nodeId, entityProvider.getNode(nodeId));
		}
	}

	public static void putNodes(OsmRelation relation,
			TLongObjectMap<OsmNode> nodes, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Node) {
				long nodeId = member.getId();
				nodes.put(nodeId, entityProvider.getNode(nodeId));
			}
		}
	}

	public static void putWays(OsmRelation relation,
			TLongObjectMap<OsmWay> ways, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Way) {
				long wayId = member.getId();
				ways.put(wayId, entityProvider.getWay(wayId));
			}
		}
	}

	public static void putWayNodes(OsmRelation relation,
			TLongObjectMap<OsmNode> additionalNodes,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Way) {
				long wayId = member.getId();
				OsmWay way = entityProvider.getWay(wayId);
				putNodes(way, additionalNodes, entityProvider);
			}
		}
	}

	public static void writeNodes(TLongObjectMap<OsmNode> map,
			OsmOutputStream osmOutput) throws IOException
	{
		long[] ids = map.keys();
		Arrays.sort(ids);
		for (long id : ids) {
			OsmNode node = map.get(id);
			osmOutput.write(node);
		}
	}

	public static void writeWays(TLongObjectMap<OsmWay> map,
			OsmOutputStream osmOutput) throws IOException
	{
		long[] ids = map.keys();
		Arrays.sort(ids);
		for (long id : ids) {
			OsmWay way = map.get(id);
			osmOutput.write(way);
		}
	}

	public static void writeRelations(TLongObjectMap<OsmRelation> map,
			OsmOutputStream osmOutput) throws IOException
	{
		long[] ids = map.keys();
		Arrays.sort(ids);
		for (long id : ids) {
			OsmRelation relation = map.get(id);
			osmOutput.write(relation);
		}
	}

}

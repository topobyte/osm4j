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
import gnu.trove.set.TLongSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

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

	/**
	 * Retrieve all nodes of the specified way from the entityProvider and
	 * insert them into the map.
	 * 
	 * @param way
	 *            the way to retrieve nodes for.
	 * @param nodes
	 *            the map to put found nodes into.
	 * @param entityProvider
	 *            the store to query for nodes.
	 * @return the number of nodes that could not be found.
	 */
	public static int putNodes(OsmWay way, TLongObjectMap<OsmNode> nodes,
			OsmEntityProvider entityProvider)
	{
		int nMissing = 0;
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			try {
				nodes.put(nodeId, entityProvider.getNode(nodeId));
			} catch (EntityNotFoundException e) {
				nMissing++;
				continue;
			}
		}
		return nMissing;
	}

	/**
	 * Retrieve all nodes of the specified way from the entityProvider and
	 * insert them into the map if they are not already present in the specified
	 * set of identifiers. The identifiers of added nodes will also be added to
	 * the set.
	 * 
	 * @param way
	 *            the way to retrieve nodes for.
	 * @param nodes
	 *            the map to put found nodes into.
	 * @param entityProvider
	 *            the store to query for nodes.
	 * @param nodeIds
	 *            the set of identifiers to check for existing nodes.
	 * @throws EntityNotFoundException
	 */
	public static void putNodes(OsmWay way, TLongObjectMap<OsmNode> nodes,
			OsmEntityProvider entityProvider, TLongSet nodeIds)
			throws EntityNotFoundException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			if (nodeIds.contains(nodeId)) {
				continue;
			}
			nodes.put(nodeId, entityProvider.getNode(nodeId));
			nodeIds.add(nodeId);
		}
	}

	/**
	 * Retrieve all nodes directly referenced by the specified relation from the
	 * entityProvider and insert them into the map.
	 * 
	 * @param relation
	 *            the relation to retrieve nodes for.
	 * @param nodes
	 *            the map to put found nodes into.
	 * @param entityProvider
	 *            the store to query for nodes.
	 * @throws EntityNotFoundException
	 */
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

	/**
	 * Retrieve all nodes directly referenced by the specified relation from the
	 * entityProvider and insert them into the map if they are not already
	 * present in the specified set of identifiers.
	 * 
	 * @param relation
	 *            the relation to retrieve nodes for.
	 * @param nodes
	 *            the map to put found nodes into.
	 * @param entityProvider
	 *            the store to query for nodes.
	 * @param nodeIds
	 *            the set of identifiers to check for existing nodes.
	 * @param counter
	 */
	public static void putNodes(OsmRelation relation,
			TLongObjectMap<OsmNode> nodes, OsmEntityProvider entityProvider,
			TLongSet nodeIds, MissingEntityCounter counter)
	{
		int nMissing = 0;
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Node) {
				long nodeId = member.getId();
				if (nodeIds.contains(nodeId)) {
					continue;
				}
				try {
					nodes.put(nodeId, entityProvider.getNode(nodeId));
				} catch (EntityNotFoundException e) {
					nMissing++;
				}
			}
		}
		counter.addNodes(nMissing);
	}

	/**
	 * Retrieve all ways directly referenced by the specified relation from the
	 * entityProvider and insert them into the map if they are not already
	 * present in the specified set of identifiers. The identifiers of added
	 * ways will also be added to the set.
	 * 
	 * @param relation
	 *            the relation to retrieve ways for.
	 * @param ways
	 *            the map to put found ways into.
	 * @param entityProvider
	 *            the store to query for ways.
	 * @param wayIds
	 *            the set of identifiers to check for existing ways.
	 * @throws EntityNotFoundException
	 */
	public static void putWays(OsmRelation relation,
			TLongObjectMap<OsmWay> ways, OsmEntityProvider entityProvider,
			TLongSet wayIds) throws EntityNotFoundException
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Way) {
				long wayId = member.getId();
				if (wayIds.contains(wayId)) {
					continue;
				}
				ways.put(wayId, entityProvider.getWay(wayId));
			}
		}
	}

	/**
	 * Retrieve all ways directly referenced by the specified relation and the
	 * nodes referenced by those ways from the entityProvider and insert them
	 * into the maps.
	 * 
	 * @param relation
	 *            the relation to retrieve ways and way-nodes for.
	 * @param nodes
	 *            the map to put found way-nodes into.
	 * @param ways
	 *            the map to put found ways into.
	 * @param entityProvider
	 *            the store to query for ways and nodes.
	 * @throws EntityNotFoundException
	 */
	public static void putWaysAndWayNodes(OsmRelation relation,
			TLongObjectMap<OsmNode> nodes, TLongObjectMap<OsmWay> ways,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Way) {
				long wayId = member.getId();
				if (ways.containsKey(wayId)) {
					continue;
				}
				OsmWay way = entityProvider.getWay(wayId);
				ways.put(wayId, way);
				putNodes(way, nodes, entityProvider);
			}
		}
	}

	/**
	 * Retrieve all ways directly referenced by the specified relation and the
	 * nodes referenced by those ways from the entityProvider and insert them
	 * into the maps if they are not already present in the specified sets of
	 * identifiers.
	 * 
	 * @param relation
	 *            the relation to retrieve ways and way-nodes for.
	 * @param nodes
	 *            the map to put found way-nodes into.
	 * @param ways
	 *            the map to put found ways into.
	 * @param entityProvider
	 *            the store to query for ways and nodes.
	 * @param wayIds
	 *            the set of identifiers to check for existing ways.
	 * @param counter
	 */
	public static void putWaysAndWayNodes(OsmRelation relation,
			TLongObjectMap<OsmNode> nodes, TLongObjectMap<OsmWay> ways,
			OsmEntityProvider entityProvider, TLongSet wayIds,
			MissingEntityCounter counter)
	{
		int nMissingWays = 0;
		int nMissingWayNodes = 0;
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Way) {
				long wayId = member.getId();
				if (wayIds.contains(wayId)) {
					continue;
				}
				OsmWay way;
				try {
					way = entityProvider.getWay(wayId);
					ways.put(wayId, way);
					nMissingWayNodes += putNodes(way, nodes, entityProvider);
				} catch (EntityNotFoundException e) {
					nMissingWays++;
				}
			}
		}
		counter.addWays(nMissingWays);
		counter.addWayNodes(nMissingWayNodes);
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

	public static boolean anyNodeContainedIn(OsmWay way, TLongSet nodeIds)
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			if (nodeIds.contains(way.getNodeId(i))) {
				return true;
			}
		}
		return false;
	}

	public static boolean anyMemberContainedIn(OsmRelation relation,
			TLongSet nodeIds, TLongSet wayIds)
	{
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Node
					&& nodeIds.contains(member.getId())
					|| member.getType() == EntityType.Way
					&& wayIds.contains(member.getId())) {
				return true;
			}
		}
		return false;
	}

	public static boolean anyMemberContainedIn(
			Collection<OsmRelation> relations, TLongSet nodeIds, TLongSet wayIds)
	{
		for (OsmRelation relation : relations) {
			if (anyMemberContainedIn(relation, nodeIds, wayIds)) {
				return true;
			}
		}
		return false;
	}

}

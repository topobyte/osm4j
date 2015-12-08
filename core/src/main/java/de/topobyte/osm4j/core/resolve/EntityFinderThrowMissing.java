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

package de.topobyte.osm4j.core.resolve;

import gnu.trove.TLongCollection;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public class EntityFinderThrowMissing extends AbstractEntityFinder
{

	private OsmEntityProvider entityProvider;

	public EntityFinderThrowMissing(OsmEntityProvider entityProvider)
	{
		this.entityProvider = entityProvider;
	}

	@Override
	public List<OsmNode> findNodes(TLongCollection ids)
			throws EntityNotFoundException
	{
		List<OsmNode> nodes = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			nodes.add(entityProvider.getNode(idIterator.next()));
		}
		return nodes;
	}

	@Override
	public List<OsmWay> findWays(TLongCollection ids)
			throws EntityNotFoundException
	{
		List<OsmWay> ways = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			ways.add(entityProvider.getWay(idIterator.next()));
		}
		return ways;
	}

	@Override
	public List<OsmRelation> findRelations(TLongCollection ids)
			throws EntityNotFoundException
	{
		List<OsmRelation> relations = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			relations.add(entityProvider.getRelation(idIterator.next()));
		}
		return relations;
	}

	@Override
	public void findWayNodes(OsmWay way, Collection<OsmNode> outNodes)
			throws EntityNotFoundException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			outNodes.add(entityProvider.getNode(way.getNodeId(i)));
		}
	}

	@Override
	public void findMemberNodes(OsmRelation relation, Set<OsmNode> outNodes)
			throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			addMember(member, outNodes, null, null, entityProvider);
		}
	}

	@Override
	public void findMemberWays(OsmRelation relation, Set<OsmWay> outWays)
			throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			addMember(member, null, outWays, null, entityProvider);
		}
	}

	@Override
	public void findMemberWays(OsmRelation relation, MultiSet<OsmWay> outWays)
			throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			if (member.getType() == EntityType.Way) {
				outWays.add(entityProvider.getWay(member.getId()));
			}
		}
	}

	@Override
	public void findMemberRelations(OsmRelation relation,
			Set<OsmRelation> outRelations) throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			addMember(member, null, null, outRelations, entityProvider);
		}
	}

	@Override
	public void findMemberRelationsRecursively(OsmRelation relation,
			Set<OsmRelation> outRelations) throws EntityNotFoundException
	{
		Deque<OsmRelation> queue = new LinkedList<>();
		queue.add(relation);
		findMemberRelationsRecursively(queue, outRelations);
	}

	@Override
	public void findMemberRelationsRecursively(
			Collection<OsmRelation> relations, Set<OsmRelation> outRelations)
			throws EntityNotFoundException
	{
		Deque<OsmRelation> queue = new LinkedList<>();
		queue.addAll(relations);
		findMemberRelationsRecursively(queue, outRelations);
	}

	private void findMemberRelationsRecursively(Deque<OsmRelation> queue,
			Set<OsmRelation> outRelations) throws EntityNotFoundException
	{
		TLongSet ids = new TLongHashSet();
		while (!queue.isEmpty()) {
			OsmRelation relation = queue.remove();
			for (OsmRelationMember member : OsmModelUtil
					.membersAsList(relation)) {
				if (member.getType() != EntityType.Relation) {
					continue;
				}
				long id = member.getId();
				if (ids.contains(id)) {
					continue;
				}
				ids.add(id);

				OsmRelation child = entityProvider.getRelation(id);
				outRelations.add(child);
				queue.add(child);
			}
		}
	}

	@Override
	public void findMemberNodesAndWays(OsmRelation relation,
			Set<OsmNode> outNodes, Set<OsmWay> outWays)
			throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			addMember(member, outNodes, outWays, null, entityProvider);
		}
	}

	@Override
	public void findMemberNodesAndWayNodes(OsmRelation relation,
			Set<OsmNode> outNodes) throws EntityNotFoundException
	{
		Set<OsmWay> ways = new HashSet<>();

		findMemberNodesAndWays(relation, outNodes, ways);

		findWayNodes(ways, outNodes);
	}

}

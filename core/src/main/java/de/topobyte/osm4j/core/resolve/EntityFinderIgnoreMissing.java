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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public class EntityFinderIgnoreMissing extends AbstractEntityFinder
{

	private OsmEntityProvider entityProvider;

	public EntityFinderIgnoreMissing(OsmEntityProvider entityProvider)
	{
		this.entityProvider = entityProvider;
	}

	@Override
	public List<OsmNode> findNodes(TLongCollection ids,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		List<OsmNode> nodes = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			try {
				nodes.add(entityProvider.getNode(idIterator.next()));
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
		return nodes;
	}

	@Override
	public List<OsmWay> findWays(TLongCollection ids,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		List<OsmWay> ways = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			try {
				ways.add(entityProvider.getWay(idIterator.next()));
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
		return ways;
	}

	@Override
	public List<OsmRelation> findRelations(TLongCollection ids,
			OsmEntityProvider entityProvider)
	{
		List<OsmRelation> relations = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			try {
				relations.add(entityProvider.getRelation(idIterator.next()));
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
		return relations;
	}

	@Override
	public void findWayNodes(OsmWay way, Collection<OsmNode> outNodes)
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			try {
				outNodes.add(entityProvider.getNode(way.getNodeId(i)));
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
	}

	@Override
	public void findMemberNodes(OsmRelation relation, Set<OsmNode> outNodes)
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			try {
				addMember(member, outNodes, null, null, entityProvider);
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
	}

	@Override
	public void findMemberWays(OsmRelation relation, Set<OsmWay> outWays)
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			try {
				addMember(member, null, outWays, null, entityProvider);
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
	}

	@Override
	public void findMemberRelations(OsmRelation relation,
			Set<OsmRelation> outRelations) throws EntityNotFoundException
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			try {
				addMember(member, null, null, outRelations, entityProvider);
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
		}
	}

	@Override
	public void findMemberNodesAndWays(OsmRelation relation,
			Set<OsmNode> outNodes, Set<OsmWay> outWays)
	{
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			try {
				addMember(member, outNodes, outWays, null, entityProvider);
			} catch (EntityNotFoundException e) {
				// ignore silently
			}
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

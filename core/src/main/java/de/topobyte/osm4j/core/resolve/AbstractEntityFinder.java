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

import java.util.Collection;
import java.util.Set;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public abstract class AbstractEntityFinder implements EntityFinder
{

	@Override
	public void findWayNodes(Collection<OsmWay> ways,
			Collection<OsmNode> outNodes) throws EntityNotFoundException
	{
		for (OsmWay way : ways) {
			findWayNodes(way, outNodes);
		}
	}

	@Override
	public void findMemberNodes(Collection<OsmRelation> relations,
			Set<OsmNode> outNodes) throws EntityNotFoundException
	{
		for (OsmRelation relation : relations) {
			findMemberNodes(relation, outNodes);
		}
	}

	@Override
	public void findMemberWays(Collection<OsmRelation> relations,
			Set<OsmWay> outWays) throws EntityNotFoundException
	{
		for (OsmRelation relation : relations) {
			findMemberWays(relation, outWays);
		}
	}

	@Override
	public void findMemberRelations(Collection<OsmRelation> relations,
			Set<OsmRelation> outRelations) throws EntityNotFoundException
	{
		for (OsmRelation relation : relations) {
			findMemberRelations(relation, outRelations);
		}
	}

	@Override
	public void findMemberNodesAndWays(Collection<OsmRelation> relations,
			Set<OsmNode> outNodes, Set<OsmWay> outWays)
			throws EntityNotFoundException
	{
		for (OsmRelation relation : relations) {
			findMemberNodesAndWays(relation, outNodes, outWays);
		}
	}

	@Override
	public void findMemberNodesAndWayNodes(Collection<OsmRelation> relations,
			Set<OsmNode> outNodes) throws EntityNotFoundException
	{
		for (OsmRelation relation : relations) {
			findMemberNodesAndWayNodes(relation, outNodes);
		}
	}

	protected void addMember(OsmRelationMember member,
			Collection<OsmNode> outNodes, Collection<OsmWay> outWays,
			Collection<OsmRelation> outRelations,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		if (member.getType() == EntityType.Node) {
			if (outNodes != null) {
				outNodes.add(entityProvider.getNode(member.getId()));
			}
		} else if (member.getType() == EntityType.Way) {
			if (outWays != null) {
				outWays.add(entityProvider.getWay(member.getId()));
			}
		} else if (member.getType() == EntityType.Relation) {
			if (outRelations != null) {
				outRelations.add(entityProvider.getRelation(member.getId()));
			}
		}
	}

}

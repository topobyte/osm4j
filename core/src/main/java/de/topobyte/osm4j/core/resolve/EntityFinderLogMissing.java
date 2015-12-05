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
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public abstract class EntityFinderLogMissing extends AbstractEntityFinder
{

	static final Logger logger = LoggerFactory
			.getLogger(EntityFinderLogMissing.class);

	private OsmEntityProvider entityProvider;

	public EntityFinderLogMissing(OsmEntityProvider entityProvider)
	{
		this.entityProvider = entityProvider;
	}

	protected abstract void log(String message);

	private void logWayNodeNotFound(OsmWay way, long nodeId)
	{
		String message = String.format(
				"Unable to find way node: way id %d, node id %d", way.getId(),
				nodeId);
		log(message);
	}

	private void logMemberNotFound(OsmRelation relation,
			OsmRelationMember member)
	{
		String message = String.format(
				"Unable to find member: relation id %d, member %s:%d",
				relation.getId(), member.getType().toString(), member.getId());
		log(message);
	}

	@Override
	public void findWayNodes(OsmWay way, Collection<OsmNode> outNodes)
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			try {
				outNodes.add(entityProvider.getNode(nodeId));
			} catch (EntityNotFoundException e) {
				logWayNodeNotFound(way, nodeId);
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
				logMemberNotFound(relation, member);
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
				logMemberNotFound(relation, member);
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
				logMemberNotFound(relation, member);
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
				logMemberNotFound(relation, member);
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

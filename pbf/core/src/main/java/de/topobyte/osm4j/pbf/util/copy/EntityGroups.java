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

package de.topobyte.osm4j.pbf.util.copy;

import java.util.ArrayList;
import java.util.List;

import crosby.binary.Osmformat.PrimitiveBlock;
import crosby.binary.Osmformat.PrimitiveGroup;
import de.topobyte.osm4j.core.model.iface.EntityType;

public class EntityGroups
{

	private List<PrimitiveGroup> nodeGroups = new ArrayList<>();
	private List<PrimitiveGroup> wayGroups = new ArrayList<>();
	private List<PrimitiveGroup> relationGroups = new ArrayList<>();

	public static EntityGroups splitEntities(PrimitiveBlock primBlock)
	{
		EntityGroups groups = new EntityGroups();

		for (int i = 0; i < primBlock.getPrimitivegroupCount(); i++) {
			PrimitiveGroup group = primBlock.getPrimitivegroup(i);

			if (group.getNodesCount() > 0 || group.hasDense()) {
				groups.nodeGroups.add(copyNodesIntoGroup(group));
			}
			if (group.getWaysCount() > 0) {
				groups.wayGroups.add(copyWaysIntoGroup(group));
			}
			if (group.getRelationsCount() > 0) {
				groups.relationGroups.add(copyRelationsIntoGroup(group));
			}
		}

		return groups;
	}

	private static PrimitiveGroup copyNodesIntoGroup(PrimitiveGroup group)
	{
		PrimitiveGroup.Builder builder = PrimitiveGroup.newBuilder();
		if (group.getNodesCount() > 0) {
			builder.addAllNodes(group.getNodesList());
		}
		if (group.hasDense()) {
			builder.setDense(group.getDense());
		}
		return builder.build();
	}

	private static PrimitiveGroup copyWaysIntoGroup(PrimitiveGroup group)
	{
		PrimitiveGroup.Builder builder = PrimitiveGroup.newBuilder();
		builder.addAllWays(group.getWaysList());
		return builder.build();
	}

	private static PrimitiveGroup copyRelationsIntoGroup(PrimitiveGroup group)
	{
		PrimitiveGroup.Builder builder = PrimitiveGroup.newBuilder();
		builder.addAllRelations(group.getRelationsList());
		return builder.build();
	}

	public List<PrimitiveGroup> getNodeGroups()
	{
		return nodeGroups;
	}

	public List<PrimitiveGroup> getWayGroups()
	{
		return wayGroups;
	}

	public List<PrimitiveGroup> getRelationGroups()
	{
		return relationGroups;
	}

	public List<PrimitiveGroup> getGroups(EntityType type)
	{
		switch (type) {
		case Node:
			return nodeGroups;
		case Way:
			return wayGroups;
		case Relation:
			return relationGroups;
		default:
			return null;
		}
	}

}

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

package de.topobyte.osm4j.testing;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.testing.model.TestBounds;
import de.topobyte.osm4j.testing.model.TestMetadata;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestRelationMember;
import de.topobyte.osm4j.testing.model.TestTag;
import de.topobyte.osm4j.testing.model.TestWay;

public class EntityHelper
{

	public static TestBounds clone(OsmBounds bounds)
	{
		return new TestBounds(bounds.getLeft(), bounds.getRight(),
				bounds.getTop(), bounds.getBottom());
	}

	public static TestNode clone(OsmNode node)
	{
		List<TestTag> tags = cloneTags(node);
		TestMetadata metadata = cloneMetadata(node);
		return node(node, tags, metadata);
	}

	public static TestWay clone(OsmWay way)
	{
		List<TestTag> tags = cloneTags(way);
		TLongList nodes = cloneNodeReferences(way);
		TestMetadata metadata = cloneMetadata(way);
		return new TestWay(way.getId(), nodes, tags, metadata);
	}

	public static TestRelation clone(OsmRelation relation)
	{
		List<TestTag> tags = cloneTags(relation);
		List<TestRelationMember> members = cloneMembers(relation);
		TestMetadata metadata = cloneMetadata(relation);
		return new TestRelation(relation.getId(), members, tags, metadata);
	}

	private static TestNode node(OsmNode node, List<TestTag> tags,
			TestMetadata metadata)
	{
		return new TestNode(node.getId(), node.getLongitude(),
				node.getLatitude(), tags, metadata);
	}

	private static TestMetadata cloneMetadata(OsmEntity entity)
	{
		OsmMetadata metadata = entity.getMetadata();
		if (metadata == null) {
			return null;
		}
		return new TestMetadata(metadata.getVersion(), metadata.getTimestamp(),
				metadata.getUid(), metadata.getUser(), metadata.getChangeset());
	}

	private static List<TestTag> cloneTags(OsmEntity entity)
	{
		List<TestTag> copy = new ArrayList<>();
		for (OsmTag tag : OsmModelUtil.getTagsAsList(entity)) {
			copy.add(new TestTag(tag.getKey(), tag.getValue()));
		}
		return copy;
	}

	private static TLongList cloneNodeReferences(OsmWay way)
	{
		TLongList nodes = new TLongArrayList(way.getNumberOfNodes());
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			nodes.add(way.getNodeId(i));
		}
		return nodes;
	}

	private static List<TestRelationMember> cloneMembers(OsmRelation relation)
	{
		List<TestRelationMember> members = new ArrayList<>(
				relation.getNumberOfMembers());
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			members.add(new TestRelationMember(member.getId(),
					member.getType(), member.getRole()));
		}
		return members;
	}

}

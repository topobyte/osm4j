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

package de.topobyte.osm4j.core.model.util;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;

public class ImplUtil
{

	public static Node clone(OsmNode node)
	{
		List<? extends OsmTag> tags = cloneTags(node);
		OsmMetadata metadata = cloneMetadata(node);
		return new Node(node.getId(), node.getLongitude(), node.getLatitude(),
				tags, metadata);
	}

	public static Way clone(OsmWay way)
	{
		List<? extends OsmTag> tags = cloneTags(way);
		OsmMetadata metadata = cloneMetadata(way);
		TLongList nodes = new TLongArrayList(way.getNumberOfNodes());
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			nodes.add(way.getNodeId(i));
		}
		return new Way(way.getId(), nodes, tags, metadata);
	}

	public static Relation clone(OsmRelation relation)
	{
		List<? extends OsmTag> tags = cloneTags(relation);
		OsmMetadata metadata = cloneMetadata(relation);

		List<RelationMember> members = new ArrayList<>(
				relation.getNumberOfMembers());
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			members.add(new RelationMember(member.getId(), member.getType(),
					member.getRole()));
		}
		return new Relation(relation.getId(), members, tags, metadata);
	}

	private static OsmMetadata cloneMetadata(OsmEntity entity)
	{
		OsmMetadata metadata = entity.getMetadata();
		if (metadata == null) {
			return null;
		}
		return new Metadata(metadata.getVersion(), metadata.getTimestamp(),
				metadata.getUid(), metadata.getUser(), metadata.getChangeset());
	}

	private static List<? extends OsmTag> cloneTags(OsmEntity entity)
	{
		List<Tag> tags = new ArrayList<>();
		for (int i = 0; i < entity.getNumberOfTags(); i++) {
			OsmTag tag = entity.getTag(i);
			tags.add(new Tag(tag.getKey(), tag.getValue()));
		}
		return tags;
	}

}

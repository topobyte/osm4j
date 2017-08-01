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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmModelUtil
{

	/**
	 * Construct a Map containing all tags of this entity.
	 * 
	 * @param entity
	 *            an osm entity.
	 * @return the entity's tags as a map.
	 */
	public static Map<String, String> getTagsAsMap(OsmEntity entity)
	{
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < entity.getNumberOfTags(); i++) {
			OsmTag tag = entity.getTag(i);
			map.put(tag.getKey(), tag.getValue());
		}
		return map;
	}

	public static List<? extends OsmTag> getTagsAsList(OsmEntity entity)
	{
		List<OsmTag> list = new ArrayList<>();
		for (int i = 0; i < entity.getNumberOfTags(); i++) {
			OsmTag tag = entity.getTag(i);
			list.add(tag);
		}
		return list;
	}

	public static TLongList nodesAsList(OsmWay way)
	{
		TLongList ids = new TLongArrayList();
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			ids.add(way.getNodeId(i));
		}
		return ids;
	}

	public static List<OsmRelationMember> membersAsList(OsmRelation relation)
	{
		List<OsmRelationMember> members = new ArrayList<>();
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			members.add(relation.getMember(i));
		}
		return members;
	}

	/**
	 * Determine whether a way is closed, i.e. the first node has the same id as
	 * the last node. Empty ways and ways with only a single node are considered
	 * closed.
	 * 
	 * @param way
	 *            a way to check
	 * @return whether the specified way is closed
	 */
	public static boolean isClosed(OsmWay way)
	{
		int n = way.getNumberOfNodes();
		// empty ways and ways with a single node are trivially closed
		if (n < 2) {
			return true;
		}
		long id0 = way.getNodeId(0);
		long idN = way.getNodeId(n - 1);
		return id0 == idN;
	}

}

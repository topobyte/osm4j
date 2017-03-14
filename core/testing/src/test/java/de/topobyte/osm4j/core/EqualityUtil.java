// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.core;

import java.util.Map;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public class EqualityUtil
{

	public static boolean equals(OsmNode a, OsmNode b)
	{
		if (a.getId() != b.getId()) {
			return false;
		}
		if (a.getLatitude() != b.getLatitude()) {
			return false;
		}
		if (a.getLongitude() != b.getLongitude()) {
			return false;
		}
		Map<String, String> tagsA = OsmModelUtil.getTagsAsMap(a);
		Map<String, String> tagsB = OsmModelUtil.getTagsAsMap(b);
		return tagsA.equals(tagsB);
	}

	public static boolean equals(OsmWay a, OsmWay b)
	{
		if (a.getId() != b.getId()) {
			return false;
		}
		int n = a.getNumberOfNodes();
		if (n != b.getNumberOfNodes()) {
			return false;
		}
		for (int i = 0; i < n; i++) {
			if (a.getNodeId(i) != b.getNodeId(i)) {
				return false;
			}
		}
		Map<String, String> tagsA = OsmModelUtil.getTagsAsMap(a);
		Map<String, String> tagsB = OsmModelUtil.getTagsAsMap(b);
		return tagsA.equals(tagsB);
	}

	public static boolean equals(OsmRelation a, OsmRelation b)
	{
		if (a.getId() != b.getId()) {
			return false;
		}
		int n = a.getNumberOfMembers();
		if (n != b.getNumberOfMembers()) {
			return false;
		}
		for (int i = 0; i < n; i++) {
			OsmRelationMember mA = a.getMember(i);
			OsmRelationMember mB = b.getMember(i);
			if (mA.getId() != mB.getId() || mA.getType() != mB.getType()
					|| !mA.getRole().equals(mB.getRole())) {
				return false;
			}
		}
		Map<String, String> tagsA = OsmModelUtil.getTagsAsMap(a);
		Map<String, String> tagsB = OsmModelUtil.getTagsAsMap(b);
		return tagsA.equals(tagsB);
	}

}

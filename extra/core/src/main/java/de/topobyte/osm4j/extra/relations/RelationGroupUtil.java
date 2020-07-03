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

package de.topobyte.osm4j.extra.relations;

import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class RelationGroupUtil
{

	public static int groupSize(Group group, OsmEntityProvider data)
	{
		int n = 0;
		for (long member : group.getRelationIds().toArray())
			try {
				OsmRelation relation = data.getRelation(member);
				n += relation.getNumberOfMembers();
			} catch (Exception e) {
				// continue
			}
		return n;
	}

}

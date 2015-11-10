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

package de.topobyte.osm4j.extra.datatree.ways;

import java.util.Comparator;

import de.topobyte.osm4j.core.model.iface.OsmWay;

public class WayNodeIdComparator implements Comparator<OsmWay>
{

	@Override
	public int compare(OsmWay o1, OsmWay o2)
	{
		long id1 = o1.getNodeId(0);
		long id2 = o2.getNodeId(0);
		int cmp = Long.compare(id1, id2);
		if (cmp != 0) {
			return cmp;
		}
		return Long.compare(o1.getId(), o2.getId());
	}

}

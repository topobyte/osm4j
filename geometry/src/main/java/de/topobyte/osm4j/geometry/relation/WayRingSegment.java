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

package de.topobyte.osm4j.geometry.relation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.model.iface.OsmWay;

public class WayRingSegment
{

	final static Logger logger = LoggerFactory.getLogger(WayRingSegment.class);

	private OsmWay way;
	private boolean reverse;

	public WayRingSegment(OsmWay way, boolean reverse)
	{
		this.way = way;
		this.reverse = reverse;
	}

	public OsmWay getWay()
	{
		return way;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof WayRingSegment) {
			WayRingSegment other = (WayRingSegment) o;
			return other.getWay().equals(way);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return (int) way.getId();
	}

	public int getNumberOfNodes()
	{
		return way.getNumberOfNodes();
	}

	public long getNodeId(int n)
	{
		if (!reverse) {
			return way.getNodeId(n);
		} else {
			return way.getNodeId(way.getNumberOfNodes() - 1 - n);
		}
	}

}

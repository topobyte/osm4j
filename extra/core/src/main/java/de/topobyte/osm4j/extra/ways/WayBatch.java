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

package de.topobyte.osm4j.extra.ways;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.batch.AbstractBatch;

public class WayBatch extends AbstractBatch<OsmWay>
{

	private int maxWays;
	private int maxWayNodes;

	private int wayNodes = 0;

	public WayBatch(int maxWays, int maxWayNodes)
	{
		this.maxWays = maxWays;
		this.maxWayNodes = maxWayNodes;
	}

	@Override
	public void clear()
	{
		super.clear();
		wayNodes = 0;
	}

	@Override
	public boolean fits(OsmWay way)
	{
		if (elements.isEmpty()) {
			return true;
		}
		if (elements.size() < maxWays
				&& wayNodes + way.getNumberOfNodes() <= maxWayNodes) {
			return true;
		}
		return false;
	}

	@Override
	public void add(OsmWay way)
	{
		super.add(way);
		wayNodes += way.getNumberOfNodes();
	}

	@Override
	public boolean isFull()
	{
		return elements.size() == maxWays || wayNodes == maxWayNodes;
	}

}

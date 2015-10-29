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

package de.topobyte.osm4j.tbo.batching;

import de.topobyte.osm4j.core.model.iface.OsmWay;

public class WayNodeCountBatchBuilder implements BatchBuilder<OsmWay>
{

	private int maxReferences;
	private int counter = 0;

	public WayNodeCountBatchBuilder(int maxReferences)
	{
		this.maxReferences = maxReferences;
	}

	@Override
	public void add(OsmWay element)
	{
		counter += element.getNumberOfNodes();
	}

	@Override
	public boolean full()
	{
		return counter >= maxReferences;
	}

	@Override
	public boolean fits(OsmWay element)
	{
		return counter + element.getNumberOfNodes() <= maxReferences;
	}

	@Override
	public void clear()
	{
		counter = 0;
	}

	@Override
	public int bufferSizeHint()
	{
		return maxReferences / 10;
	}

}

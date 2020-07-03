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

package de.topobyte.osm4j.core.access.wrapper;

import de.topobyte.osm4j.core.access.OsmElementCounter;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;

public class OsmElementCounterIteratorAdapter implements OsmElementCounter
{

	private OsmIterator iterator;

	public OsmElementCounterIteratorAdapter(OsmIterator iterator)
	{
		this.iterator = iterator;
	}

	private int numNodes = 0;
	private int numWays = 0;
	private int numRelations = 0;

	@Override
	public void count()
	{
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			switch (container.getType()) {
			case Node:
				numNodes++;
				break;
			case Way:
				numWays++;
				break;
			case Relation:
				numRelations++;
				break;
			}
		}
	}

	@Override
	public long getNumberOfNodes()
	{
		return numNodes;
	}

	@Override
	public long getNumberOfWays()
	{
		return numWays;
	}

	@Override
	public long getNumberOfRelations()
	{
		return numRelations;
	}

	@Override
	public long getTotalNumberOfElements()
	{
		return numNodes + numWays + numRelations;
	}

	@Override
	public long getNumberOfElements(EntityType type)
	{
		switch (type) {
		default:
			return 0;
		case Node:
			return numNodes;
		case Way:
			return numWays;
		case Relation:
			return numRelations;
		}
	}

}

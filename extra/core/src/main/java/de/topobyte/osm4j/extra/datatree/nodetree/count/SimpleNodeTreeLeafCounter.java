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

package de.topobyte.osm4j.extra.datatree.nodetree.count;

import java.io.IOException;

import com.slimjars.dist.gnu.trove.map.TLongLongMap;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;

public class SimpleNodeTreeLeafCounter implements NodeTreeLeafCounter
{

	private OsmIterator iterator;

	private IteratorNodeTreeLeafCounter counter;

	public SimpleNodeTreeLeafCounter(DataTree tree, Node head,
			OsmIterator iterator)
	{
		this.iterator = iterator;

		counter = new IteratorNodeTreeLeafCounter(tree, head);
	}

	@Override
	public Node getHead()
	{
		return counter.getHead();
	}

	@Override
	public TLongLongMap getCounters()
	{
		return counter.getCounters();
	}

	@Override
	public void execute() throws IOException
	{
		counter.execute(iterator);
	}

}

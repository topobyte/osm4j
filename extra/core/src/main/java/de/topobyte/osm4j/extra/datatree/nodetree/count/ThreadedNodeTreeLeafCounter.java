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

import gnu.trove.map.TLongLongMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.buffer.OsmBuffer;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;
import de.topobyte.osm4j.utils.buffer.RunnableBufferBridge;

public class ThreadedNodeTreeLeafCounter implements NodeTreeLeafCounter
{

	private OsmIterator iterator;

	private IteratorNodeTreeLeafCounter counter;

	public ThreadedNodeTreeLeafCounter(DataTree tree, Node head,
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
		count();
	}

	private void count() throws IOException
	{
		final OsmBuffer buffer = new OsmBuffer(10000, 20);
		RunnableBufferBridge bridge = new RunnableBufferBridge(iterator, buffer);

		Runnable runnableLeafCounter = new Runnable() {

			@Override
			public void run()
			{
				try {
					counter.execute(buffer);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(bridge);
		tasks.add(runnableLeafCounter);

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();
	}

}

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
import java.util.List;

import com.slimjars.dist.gnu.trove.map.TLongLongMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongLongHashMap;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;

public class IteratorNodeTreeLeafCounter
{

	private DataTree tree;
	private Node head;

	private TLongLongMap counters = new TLongLongHashMap();
	private NodeProgress counter = new NodeProgress();

	public IteratorNodeTreeLeafCounter(DataTree tree, Node head)
	{
		this.tree = tree;
		this.head = head;
	}

	public Node getHead()
	{
		return head;
	}

	public TLongLongMap getCounters()
	{
		return counters;
	}

	public void execute(OsmIterator input) throws IOException
	{
		counter.printTimed(1000);

		try {
			count(input);
		} finally {
			counter.stop();
		}
	}

	private void count(OsmIterator iterator) throws IOException
	{
		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				OsmNode node = (OsmNode) entityContainer.getEntity();
				findLeafsAndIncrementCounters(node);
				counter.increment();
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}
	}

	private void findLeafsAndIncrementCounters(OsmNode node)
	{
		List<Node> leafs = tree.query(head, node.getLongitude(),
				node.getLatitude());
		for (Node leaf : leafs) {
			long path = leaf.getPath();
			counters.put(path, counters.get(path) + 1);
		}
	}

}

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

package de.topobyte.osm4j.utils.merge.sorted;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.utils.merge.AbstractIdMerge;

public class AbstractSortedIdMerge extends AbstractIdMerge
{

	public AbstractSortedIdMerge(Collection<OsmIdIterator> inputs)
	{
		super(inputs);
	}

	// This class is used to store the input sources in a priority queue and
	// stores the next element available on a source

	protected class Input
	{

		OsmIdIterator iterator;
		long currentId;

		public Input(OsmIdIterator iterator)
		{
			this.iterator = iterator;
		}

	}

	protected Input createItem(long id, OsmIdIterator iterator)
	{
		Input item = new Input(iterator);
		item.currentId = id;
		return item;
	}

	// This comparator is used to order input sources within the priority
	// queues.

	private class InputComparator implements Comparator<Input>
	{

		@Override
		public int compare(Input o1, Input o2)
		{
			return Long.compare(o1.currentId, o2.currentId);
		}

	}

	// One priority queue for each entity type
	protected PriorityQueue<Input> nodeItems = new PriorityQueue<>(2,
			new InputComparator());
	protected PriorityQueue<Input> wayItems = new PriorityQueue<>(2,
			new InputComparator());
	protected PriorityQueue<Input> relationItems = new PriorityQueue<>(2,
			new InputComparator());

	protected boolean advanceNodeItem(Input item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		IdContainer container = item.iterator.next();

		if (container.getType() == EntityType.Node) {
			item.currentId = container.getId();
			if (putBack) {
				nodeItems.add(item);
			}
			return true;
		} else if (container.getType() == EntityType.Way) {
			Input newItem = new Input(item.iterator);
			newItem.currentId = container.getId();
			wayItems.add(newItem);
		} else if (container.getType() == EntityType.Relation) {
			Input newItem = new Input(item.iterator);
			newItem.currentId = container.getId();
			relationItems.add(newItem);
		}
		return false;
	}

	protected boolean advanceWayItem(Input item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		IdContainer container = item.iterator.next();
		if (container.getType() == EntityType.Way) {
			item.currentId = container.getId();
			if (putBack) {
				wayItems.add(item);
			}
			return true;
		} else if (container.getType() == EntityType.Relation) {
			Input newItem = new Input(item.iterator);
			newItem.currentId = container.getId();
			relationItems.add(newItem);
		}
		return false;
	}

	protected boolean advanceRelationItem(Input item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		IdContainer container = item.iterator.next();
		if (container.getType() == EntityType.Relation) {
			item.currentId = container.getId();
			if (putBack) {
				relationItems.add(item);
			}
			return true;
		}
		return false;
	}

}

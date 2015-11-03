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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class Merge
{

	private OsmOutputStream output;
	private Collection<OsmIterator> inputs;

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * output. The merging algorithm expects the input data to be in default
	 * order, i.e. a sequence of nodes, followed by a sequence of ways, followed
	 * by a sequence of relations, each sequence ordered by their object's
	 * identifiers.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 */
	public Merge(OsmOutputStream output, Collection<OsmIterator> inputs)
	{
		this.output = output;
		this.inputs = inputs;
	}

	// This class is used to store the input sources in a priority queue and
	// stores the next element available on a source
	private class InputSource
	{
		OsmIterator iterator;
		EntityType currentType;
		long currentId;
		OsmEntity currentEntity;
	}

	// This comparator is used to order input sources within the priority
	// queues.
	private class InputComparator implements Comparator<InputSource>
	{

		@Override
		public int compare(InputSource o1, InputSource o2)
		{
			return Long.compare(o1.currentId, o2.currentId);
		}

	}

	// One priority queue for each entity type
	private PriorityQueue<InputSource> nodeItems = new PriorityQueue<>(2,
			new InputComparator());
	private PriorityQueue<InputSource> wayItems = new PriorityQueue<>(2,
			new InputComparator());
	private PriorityQueue<InputSource> relationItems = new PriorityQueue<>(2,
			new InputComparator());

	// Remember the last id written per entity type to skip duplicates
	private long lastId = -1;

	public void run() throws IOException
	{
		// First initialize the input sources and put into the priority queues
		prepare();
		// Then iterate the sources as long as input is available
		iterate();
		// Finally, generate a complete() event on the output
		output.complete();
	}

	private void prepare() throws IOException
	{
		for (OsmIterator iterator : inputs) {
			if (!iterator.hasNext()) {
				continue;
			}
			EntityContainer container = iterator.next();
			InputSource item = createItem(container, iterator);
			switch (item.currentType) {
			case Node:
				nodeItems.add(item);
				break;
			case Way:
				wayItems.add(item);
				break;
			case Relation:
				relationItems.add(item);
				break;
			}
		}
	}

	private void iterate() throws IOException
	{
		// More than one node source:
		// use priority queue
		while (nodeItems.size() > 1) {
			InputSource item = nodeItems.poll();
			writeNode(item);
			if (advance(item)) {
				putToBucket(item);
			}
		}

		// Just one node source left:
		// iterate until finished or ways or relations pop up
		if (nodeItems.size() == 1) {
			InputSource item = nodeItems.poll();
			while (true) {
				writeNode(item);
				if (!advance(item)) {
					break;
				}
				if (item.currentType == EntityType.Node) {
					continue;
				} else {
					putToBucket(item);
					break;
				}
			}
		}

		lastId = -1;

		// More than one way source:
		// use priority queue
		while (wayItems.size() > 1) {
			InputSource item = wayItems.poll();
			writeWay(item);
			if (advance(item)) {
				putToBucket(item);
			}
		}

		// Just one way source left:
		// iterate until finished or relations pop up
		if (wayItems.size() == 1) {
			InputSource item = wayItems.poll();
			while (true) {
				writeWay(item);
				if (!advance(item)) {
					break;
				}
				if (item.currentType == EntityType.Way) {
					continue;
				} else {
					putToBucket(item);
					break;
				}
			}
		}

		lastId = -1;

		// More than one relation source:
		// use priority queue
		while (relationItems.size() > 1) {
			InputSource item = relationItems.poll();
			writeRelation(item);
			if (advance(item)) {
				putToBucket(item);
			}
		}

		// Just one relation source left:
		// iterate until finished or ways or relations pop up
		if (relationItems.size() == 1) {
			InputSource item = relationItems.poll();
			while (true) {
				writeRelation(item);
				if (!advance(item)) {
					break;
				}
				if (item.currentType == EntityType.Relation) {
					continue;
				}
			}
		}
	}

	private void writeNode(InputSource item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write((OsmNode) item.currentEntity);
			lastId = item.currentId;
		}
	}

	private void writeWay(InputSource item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write((OsmWay) item.currentEntity);
			lastId = item.currentId;
		}
	}

	private void writeRelation(InputSource item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write((OsmRelation) item.currentEntity);
			lastId = item.currentId;
		}
	}

	private void putToBucket(InputSource item)
	{
		if (item.currentType == EntityType.Node) {
			nodeItems.add(item);
		} else if (item.currentType == EntityType.Way) {
			wayItems.add(item);
		} else if (item.currentType == EntityType.Relation) {
			relationItems.add(item);
		}
	}

	private InputSource createItem(EntityContainer container,
			OsmIterator iterator)
	{
		InputSource item = new InputSource();
		item.currentEntity = container.getEntity();
		item.currentType = container.getType();
		item.currentId = item.currentEntity.getId();
		item.iterator = iterator;
		return item;
	}

	private boolean advance(InputSource item)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		item.currentEntity = container.getEntity();
		item.currentType = container.getType();
		item.currentId = item.currentEntity.getId();
		return true;
	}

}

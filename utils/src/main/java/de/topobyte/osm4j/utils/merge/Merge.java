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

package de.topobyte.osm4j.utils.merge;

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
import de.topobyte.osm4j.utils.sort.IdComparator;

public class Merge
{

	private OsmOutputStream output;
	private Collection<OsmIterator> inputs;

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * output. The merging algorithm expects the input data to be in default
	 * order, i.e. a sequence of nodes, followed by a sequence of ways, followed
	 * by a sequence of relations. Each sequence ordered by their object's
	 * identifiers.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 */
	public Merge(OsmOutputStream output, Collection<OsmIterator> inputs)
	{
		this(output, inputs, new IdComparator());
	}

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * output. The merging algorithm expects the input data to be in default
	 * order, i.e. a sequence of nodes, followed by a sequence of ways, followed
	 * by a sequence of relations. Each sequence ordered using the specified
	 * comparator.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 * @param comparator
	 *            a Comparator used to compare elements of the same type.
	 */
	public Merge(OsmOutputStream output, Collection<OsmIterator> inputs,
			Comparator<OsmEntity> comparator)
	{
		this(output, inputs, comparator, comparator, comparator);
	}

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * output. The merging algorithm expects the input data to be in default
	 * order, i.e. a sequence of nodes, followed by a sequence of ways, followed
	 * by a sequence of relations. Each sequence ordered using the respective
	 * specified comparator.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 * @param comparatorNodes
	 *            a Comparator used to compare nodes.
	 * @param comparatorWays
	 *            a Comparator used to compare ways.
	 * @param comparatorRelations
	 *            a Comparator used to compare relations.
	 */
	public Merge(OsmOutputStream output, Collection<OsmIterator> inputs,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
	{
		this.output = output;
		this.inputs = inputs;
		this.comparatorNodes = comparatorNodes;
		this.comparatorWays = comparatorWays;
		this.comparatorRelations = comparatorRelations;
	}

	// This class is used to store the input sources in a priority queue and
	// stores the next element available on a source

	private class Input<T extends OsmEntity>
	{

		OsmIterator iterator;
		T currentEntity;
		long currentId;

		public Input(OsmIterator iterator)
		{
			this.iterator = iterator;
		}

	}

	// These comparators are used to order input sources within the priority
	// queues.

	private class InputComparatorNodes implements Comparator<Input<OsmNode>>
	{

		@Override
		public int compare(Input<OsmNode> o1, Input<OsmNode> o2)
		{
			return comparatorNodes.compare(o1.currentEntity, o2.currentEntity);
		}

	}

	private class InputComparatorWays implements Comparator<Input<OsmWay>>
	{

		@Override
		public int compare(Input<OsmWay> o1, Input<OsmWay> o2)
		{
			return comparatorWays.compare(o1.currentEntity, o2.currentEntity);
		}

	}

	private class InputComparatorRelations implements
			Comparator<Input<OsmRelation>>
	{

		@Override
		public int compare(Input<OsmRelation> o1, Input<OsmRelation> o2)
		{
			return comparatorRelations.compare(o1.currentEntity,
					o2.currentEntity);
		}

	}

	// One priority queue for each entity type
	private PriorityQueue<Input<OsmNode>> nodeItems = new PriorityQueue<>(2,
			new InputComparatorNodes());
	private PriorityQueue<Input<OsmWay>> wayItems = new PriorityQueue<>(2,
			new InputComparatorWays());
	private PriorityQueue<Input<OsmRelation>> relationItems = new PriorityQueue<>(
			2, new InputComparatorRelations());

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
			switch (container.getType()) {
			case Node:
				nodeItems.add(createItem((OsmNode) container.getEntity(),
						iterator));
				break;
			case Way:
				wayItems.add(createItem((OsmWay) container.getEntity(),
						iterator));
				break;
			case Relation:
				relationItems.add(createItem(
						(OsmRelation) container.getEntity(), iterator));
				break;
			}
		}
	}

	private void iterate() throws IOException
	{
		// More than one node source:
		// use priority queue
		while (nodeItems.size() > 1) {
			Input<OsmNode> item = nodeItems.poll();
			writeNode(item);
			advanceNodeItem(item, true);
		}

		// Just one node source left:
		// iterate until finished or ways or relations pop up
		if (nodeItems.size() == 1) {
			Input<OsmNode> item = nodeItems.poll();
			while (true) {
				writeNode(item);
				if (!advanceNodeItem(item, false)) {
					break;
				}
			}
		}

		lastId = -1;

		// More than one way source:
		// use priority queue
		while (wayItems.size() > 1) {
			Input<OsmWay> item = wayItems.poll();
			writeWay(item);
			advanceWayItem(item, true);
		}

		// Just one way source left:
		// iterate until finished or relations pop up
		if (wayItems.size() == 1) {
			Input<OsmWay> item = wayItems.poll();
			while (true) {
				writeWay(item);
				if (!advanceWayItem(item, false)) {
					break;
				}
			}
		}

		lastId = -1;

		// More than one relation source:
		// use priority queue
		while (relationItems.size() > 1) {
			Input<OsmRelation> item = relationItems.poll();
			writeRelation(item);
			advanceRelationItem(item, true);
		}

		// Just one relation source left:
		// iterate until finished or ways or relations pop up
		if (relationItems.size() == 1) {
			Input<OsmRelation> item = relationItems.poll();
			while (true) {
				writeRelation(item);
				if (!advanceRelationItem(item, false)) {
					break;
				}
			}
		}
	}

	private void writeNode(Input<OsmNode> item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write(item.currentEntity);
			lastId = item.currentId;
		}
	}

	private void writeWay(Input<OsmWay> item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write(item.currentEntity);
			lastId = item.currentId;
		}
	}

	private void writeRelation(Input<OsmRelation> item) throws IOException
	{
		if (item.currentId != lastId) {
			output.write(item.currentEntity);
			lastId = item.currentId;
		}
	}

	private <T extends OsmEntity> Input<T> createItem(T element,
			OsmIterator iterator)
	{
		Input<T> item = new Input<T>(iterator);
		item.currentEntity = element;
		item.currentId = item.currentEntity.getId();
		return item;
	}

	private boolean advanceNodeItem(Input<OsmNode> item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Node) {
			item.currentEntity = (OsmNode) entity;
			item.currentId = entity.getId();
			if (putBack) {
				nodeItems.add(item);
			}
			return true;
		} else if (container.getType() == EntityType.Way) {
			Input<OsmWay> newItem = new Input<>(item.iterator);
			newItem.currentEntity = (OsmWay) entity;
			newItem.currentId = entity.getId();
			wayItems.add(newItem);
		} else if (container.getType() == EntityType.Relation) {
			Input<OsmRelation> newItem = new Input<>(item.iterator);
			newItem.currentEntity = (OsmRelation) entity;
			newItem.currentId = entity.getId();
			relationItems.add(newItem);
		}
		return false;
	}

	private boolean advanceWayItem(Input<OsmWay> item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Way) {
			item.currentEntity = (OsmWay) entity;
			item.currentId = entity.getId();
			if (putBack) {
				wayItems.add(item);
			}
			return true;
		} else if (container.getType() == EntityType.Relation) {
			Input<OsmRelation> newItem = new Input<>(item.iterator);
			newItem.currentEntity = (OsmRelation) entity;
			newItem.currentId = entity.getId();
			relationItems.add(newItem);
		}
		return false;
	}

	private boolean advanceRelationItem(Input<OsmRelation> item, boolean putBack)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Relation) {
			item.currentEntity = (OsmRelation) entity;
			item.currentId = entity.getId();
			if (putBack) {
				relationItems.add(item);
			}
			return true;
		}
		return false;
	}

}

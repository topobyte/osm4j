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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.sort.IdComparator;

public class MergeIterator implements OsmIterator
{

	private Collection<OsmIterator> inputs;

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * input source. The merging algorithm expects the input data to be in
	 * default order, i.e. a sequence of nodes, followed by a sequence of ways,
	 * followed by a sequence of relations. Each sequence ordered by their
	 * object's identifiers.
	 * 
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 */
	public MergeIterator(Collection<OsmIterator> inputs) throws IOException
	{
		this(inputs, new IdComparator());
	}

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * input source. The merging algorithm expects the input data to be in
	 * default order, i.e. a sequence of nodes, followed by a sequence of ways,
	 * followed by a sequence of relations. Each sequence ordered using the
	 * specified comparator.
	 * 
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 * @param comparator
	 *            a Comparator used to compare elements of the same type.
	 */
	public MergeIterator(Collection<OsmIterator> inputs,
			Comparator<OsmEntity> comparator) throws IOException
	{
		this(inputs, comparator, comparator, comparator);
	}

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * input source. The merging algorithm expects the input data to be in
	 * default order, i.e. a sequence of nodes, followed by a sequence of ways,
	 * followed by a sequence of relations. Each sequence ordered using the
	 * respective specified comparator.
	 * 
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 * @param comparatorNodes
	 *            a Comparator used to compare nodes.
	 * @param comparatorWays
	 *            a Comparator used to compare ways.
	 * @param comparatorRelations
	 *            a Comparator used to compare relations.
	 */
	public MergeIterator(Collection<OsmIterator> inputs,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
			throws IOException
	{
		this.inputs = inputs;
		this.comparatorNodes = comparatorNodes;
		this.comparatorWays = comparatorWays;
		this.comparatorRelations = comparatorRelations;

		// Initialize the input sources and put into the priority queues
		prepare();
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

	// Remember the last returned id per entity type to skip duplicates
	private long lastId = -1;

	private boolean available = false;
	private EntityType mode = null;

	private void prepare() throws IOException
	{
		for (OsmIterator iterator : inputs) {
			if (!iterator.hasNext()) {
				continue;
			}
			available = true;
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
		if (!nodeItems.isEmpty()) {
			mode = EntityType.Node;
		} else if (!wayItems.isEmpty()) {
			mode = EntityType.Way;
		} else if (!relationItems.isEmpty()) {
			mode = EntityType.Relation;
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

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasBounds()
	{
		// TODO implement
		return false;
	}

	@Override
	public OsmBounds getBounds()
	{
		// TODO implement
		return null;
	}

	@Override
	public boolean hasNext()
	{
		return available;
	}

	@Override
	public EntityContainer next()
	{
		switch (mode) {
		case Node:
			return nextNode();
		case Way:
			return nextWay();
		case Relation:
			return nextRelation();
		default:
			throw new NoSuchElementException();
		}
	}

	private EntityContainer nextNode()
	{
		Input<OsmNode> item = nodeItems.poll();
		OsmEntity current = item.currentEntity;
		lastId = current.getId();

		advanceNodeItem(item, true);
		skipDuplicateNodes();
		ensureMode();
		return new EntityContainer(EntityType.Node, current);
	}

	private EntityContainer nextWay()
	{
		Input<OsmWay> item = wayItems.poll();
		OsmEntity current = item.currentEntity;
		lastId = current.getId();

		advanceWayItem(item, true);
		skipDuplicateWays();
		ensureMode();
		return new EntityContainer(EntityType.Way, current);
	}

	private EntityContainer nextRelation()
	{
		Input<OsmRelation> item = relationItems.poll();
		OsmEntity current = item.currentEntity;
		lastId = current.getId();

		advanceRelationItem(item, true);
		skipDuplicateRelations();
		ensureMode();
		return new EntityContainer(EntityType.Relation, current);
	}

	private void skipDuplicateNodes()
	{
		while (!nodeItems.isEmpty()) {
			Input<OsmNode> item = nodeItems.peek();
			if (item.currentId != lastId) {
				break;
			}
			nodeItems.poll();
			advanceNodeItem(item, true);
			continue;
		}
	}

	private void skipDuplicateWays()
	{
		while (!wayItems.isEmpty()) {
			Input<OsmWay> item = wayItems.peek();
			if (item.currentId != lastId) {
				break;
			}
			wayItems.poll();
			advanceWayItem(item, true);
			continue;
		}
	}

	private void skipDuplicateRelations()
	{
		while (!relationItems.isEmpty()) {
			Input<OsmRelation> item = relationItems.peek();
			if (item.currentId != lastId) {
				break;
			}
			relationItems.poll();
			advanceRelationItem(item, true);
			continue;
		}
	}

	private void ensureMode()
	{
		switch (mode) {
		case Node:
			if (!nodeItems.isEmpty()) {
				return;
			} else if (!wayItems.isEmpty()) {
				mode = EntityType.Way;
				lastId = -1;
			} else if (!relationItems.isEmpty()) {
				mode = EntityType.Relation;
				lastId = -1;
			} else {
				available = false;
			}
			break;
		case Way:
			if (!wayItems.isEmpty()) {
				return;
			} else if (!relationItems.isEmpty()) {
				mode = EntityType.Relation;
				lastId = -1;
			} else {
				available = false;
			}
			break;
		case Relation:
			if (!relationItems.isEmpty()) {
				return;
			} else {
				available = false;
			}
			break;
		}
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

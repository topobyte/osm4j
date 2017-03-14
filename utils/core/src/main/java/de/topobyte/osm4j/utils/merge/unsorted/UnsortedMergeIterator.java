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

package de.topobyte.osm4j.utils.merge.unsorted;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class UnsortedMergeIterator extends AbstractUnsortedMerge implements
		OsmIterator
{

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * input source. The merging algorithm expects the input data to be in
	 * default order, i.e. a sequence of nodes, followed by a sequence of ways,
	 * followed by a sequence of relations.
	 * 
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 */
	public UnsortedMergeIterator(Collection<OsmIterator> inputs)
			throws IOException
	{
		super(inputs);

		// Initialize the input sources and put into the dequeues
		prepare();
	}

	private boolean available = false;
	private EntityType mode = null;

	private Input<OsmNode> nodeInput = null;
	private Input<OsmWay> wayInput = null;
	private Input<OsmRelation> relationInput = null;

	private void prepare() throws IOException
	{
		for (OsmIterator iterator : inputs) {
			if (!iterator.hasNext()) {
				continue;
			}
			available = true;
			EntityContainer container = iterator.next();
			OsmEntity entity = container.getEntity();
			switch (container.getType()) {
			case Node:
				nodeItems.add(createItem((OsmNode) entity, iterator));
				break;
			case Way:
				wayItems.add(createItem((OsmWay) entity, iterator));
				break;
			case Relation:
				relationItems.add(createItem((OsmRelation) entity, iterator));
				break;
			}
		}
		if (!nodeItems.isEmpty()) {
			mode = EntityType.Node;
			nodeInput = nodeItems.poll();
		} else if (!wayItems.isEmpty()) {
			mode = EntityType.Way;
			wayInput = wayItems.poll();
		} else if (!relationItems.isEmpty()) {
			mode = EntityType.Relation;
			relationInput = relationItems.poll();
		}
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
		return hasBounds;
	}

	@Override
	public OsmBounds getBounds()
	{
		return bounds;
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
		OsmNode current = nodeInput.firstEntity;
		if (!advanceNodeItem(nodeInput)) {
			ensureMode();
		}
		return new EntityContainer(EntityType.Node, current);
	}

	private EntityContainer nextWay()
	{
		OsmWay current = wayInput.firstEntity;
		if (!advanceWayItem(wayInput)) {
			ensureMode();
		}
		return new EntityContainer(EntityType.Way, current);
	}

	private EntityContainer nextRelation()
	{
		OsmRelation current = relationInput.firstEntity;
		if (!advanceRelationItem(relationInput)) {
			ensureMode();
		}
		return new EntityContainer(EntityType.Relation, current);
	}

	protected boolean advanceNodeItem(Input<OsmNode> item)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Node) {
			item.firstEntity = (OsmNode) entity;
			return true;
		} else if (container.getType() == EntityType.Way) {
			Input<OsmWay> newItem = new Input<>(item.iterator);
			newItem.firstEntity = (OsmWay) entity;
			wayItems.add(newItem);
		} else if (container.getType() == EntityType.Relation) {
			Input<OsmRelation> newItem = new Input<>(item.iterator);
			newItem.firstEntity = (OsmRelation) entity;
			relationItems.add(newItem);
		}
		return false;
	}

	protected boolean advanceWayItem(Input<OsmWay> item)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Way) {
			item.firstEntity = (OsmWay) entity;
			return true;
		} else if (container.getType() == EntityType.Relation) {
			Input<OsmRelation> newItem = new Input<>(item.iterator);
			newItem.firstEntity = (OsmRelation) entity;
			relationItems.add(newItem);
		}
		return false;
	}

	protected boolean advanceRelationItem(Input<OsmRelation> item)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		OsmEntity entity = container.getEntity();
		if (container.getType() == EntityType.Relation) {
			item.firstEntity = (OsmRelation) entity;
			return true;
		}
		return false;
	}

	private void ensureMode()
	{
		switch (mode) {
		case Node:
			if (!nodeItems.isEmpty()) {
				nodeInput = nodeItems.poll();
				return;
			} else if (!wayItems.isEmpty()) {
				mode = EntityType.Way;
				wayInput = wayItems.poll();
			} else if (!relationItems.isEmpty()) {
				mode = EntityType.Relation;
				relationInput = relationItems.poll();
			} else {
				available = false;
			}
			break;
		case Way:
			if (!wayItems.isEmpty()) {
				wayInput = wayItems.poll();
				return;
			} else if (!relationItems.isEmpty()) {
				mode = EntityType.Relation;
				relationInput = relationItems.poll();
			} else {
				available = false;
			}
			break;
		case Relation:
			if (!relationItems.isEmpty()) {
				relationInput = relationItems.poll();
				return;
			} else {
				available = false;
			}
			break;
		}
	}

}

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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;

public class SortedIdMergeIterator extends AbstractSortedIdMerge implements
		OsmIdIterator
{

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
	public SortedIdMergeIterator(Collection<OsmIdIterator> inputs)
			throws IOException
	{
		super(inputs);

		// Initialize the input sources and put into the priority queues
		prepare();
	}

	private boolean available = false;
	private EntityType mode = null;

	// Remember the last id returned per entity type to skip duplicates
	private long lastId = -1;

	private void prepare() throws IOException
	{
		for (OsmIdIterator iterator : inputs) {
			if (!iterator.hasNext()) {
				continue;
			}
			available = true;
			IdContainer container = iterator.next();
			long id = container.getId();
			switch (container.getType()) {
			case Node:
				nodeItems.add(createItem(id, iterator));
				break;
			case Way:
				wayItems.add(createItem(id, iterator));
				break;
			case Relation:
				relationItems.add(createItem(id, iterator));
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

	@Override
	public Iterator<IdContainer> iterator()
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
	public IdContainer next()
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

	private IdContainer nextNode()
	{
		Input item = nodeItems.poll();
		lastId = item.currentId;

		advanceNodeItem(item, true);
		skipDuplicateNodes();
		ensureMode();
		return new IdContainer(EntityType.Node, lastId);
	}

	private IdContainer nextWay()
	{
		Input item = wayItems.poll();
		lastId = item.currentId;

		advanceWayItem(item, true);
		skipDuplicateWays();
		ensureMode();
		return new IdContainer(EntityType.Way, lastId);
	}

	private IdContainer nextRelation()
	{
		Input item = relationItems.poll();
		lastId = item.currentId;

		advanceRelationItem(item, true);
		skipDuplicateRelations();
		ensureMode();
		return new IdContainer(EntityType.Relation, lastId);
	}

	private void skipDuplicateNodes()
	{
		while (!nodeItems.isEmpty()) {
			Input item = nodeItems.peek();
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
			Input item = wayItems.peek();
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
			Input item = relationItems.peek();
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

}

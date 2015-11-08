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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.sort.IdComparator;

public class MergeIterator extends AbstractMerge implements OsmIterator
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
		super(inputs, comparatorNodes, comparatorWays, comparatorRelations);

		// Initialize the input sources and put into the priority queues
		prepare();
	}

	private boolean available = false;
	private EntityType mode = null;

	// Remember the last id returned per entity type to skip duplicates
	private long lastId = -1;

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

}

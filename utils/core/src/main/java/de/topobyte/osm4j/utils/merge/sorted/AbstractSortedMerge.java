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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.merge.AbstractMerge;

public class AbstractSortedMerge extends AbstractMerge
{

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	public AbstractSortedMerge(Collection<OsmIterator> inputs,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
	{
		super(inputs);
		this.comparatorNodes = comparatorNodes;
		this.comparatorWays = comparatorWays;
		this.comparatorRelations = comparatorRelations;
	}

	// This class is used to store the input sources in a priority queue and
	// stores the next element available on a source

	protected class Input<T extends OsmEntity>
	{

		OsmIterator iterator;
		T currentEntity;
		long currentId;

		public Input(OsmIterator iterator)
		{
			this.iterator = iterator;
		}

	}

	protected <T extends OsmEntity> Input<T> createItem(T element,
			OsmIterator iterator)
	{
		Input<T> item = new Input<>(iterator);
		item.currentEntity = element;
		item.currentId = item.currentEntity.getId();
		return item;
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
	protected PriorityQueue<Input<OsmNode>> nodeItems = new PriorityQueue<>(2,
			new InputComparatorNodes());
	protected PriorityQueue<Input<OsmWay>> wayItems = new PriorityQueue<>(2,
			new InputComparatorWays());
	protected PriorityQueue<Input<OsmRelation>> relationItems = new PriorityQueue<>(
			2, new InputComparatorRelations());

	protected boolean advanceNodeItem(Input<OsmNode> item, boolean putBack)
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

	protected boolean advanceWayItem(Input<OsmWay> item, boolean putBack)
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

	protected boolean advanceRelationItem(Input<OsmRelation> item,
			boolean putBack)
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

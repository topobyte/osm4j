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

package de.topobyte.osm4j.utils.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class MemorySortIterator implements OsmIterator
{

	private OsmIterator input;

	private boolean ignoreDuplicates = true;

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	/**
	 * Sort the elements from a OSM input source by their id in memory and
	 * provide them as a single OSM input source in that order. Sorting is
	 * applied to elements of the same type, i.e. the output contains first
	 * nodes, then ways and then relations, each list of elements ordered by the
	 * elements' ids.
	 * 
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 */
	public MemorySortIterator(OsmIterator input)
	{
		this(input, new IdComparator());
	}

	/**
	 * Sort the elements from a OSM input source by their id in memory and
	 * provide them as a single OSM input source in that order. Sorting is
	 * applied to elements of the same type, i.e. the output contains first
	 * nodes, then ways and then relations, each list of elements sorted using
	 * the comparator.
	 * 
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 * @param comparator
	 *            a Comparator used to sort each list of elements.
	 */
	public MemorySortIterator(OsmIterator input,
			Comparator<OsmEntity> comparator)
	{
		this(input, comparator, comparator, comparator);
	}

	/**
	 * Sort the elements from a OSM input source by their id in memory and
	 * provide them as a single OSM input source in that order. Sorting is
	 * applied to elements of the same type, i.e. the output contains first
	 * nodes, then ways and then relations, each list of elements sorted using
	 * the respective comparator.
	 * 
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 * @param comparatorNodes
	 *            a Comparator used to sort the list of nodes.
	 * @param comparatorWays
	 *            a Comparator used to sort the list of ways.
	 * @param comparatorRelations
	 *            a Comparator used to sort the list of relations.
	 */
	public MemorySortIterator(OsmIterator input,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
	{
		this.input = input;
		this.comparatorNodes = comparatorNodes;
		this.comparatorWays = comparatorWays;
		this.comparatorRelations = comparatorRelations;
	}

	/**
	 * Whether objects that have the same type and id as a previously
	 * encountered element will be discarded from output.
	 */
	public boolean isIgnoreDuplicates()
	{
		return ignoreDuplicates;
	}

	/**
	 * When set to false, elements with the same id can appear multiple times in
	 * the output. When set to true, for each entity type, if there are multiple
	 * objects with the same id, only the first one will be passed to the
	 * output. Default value: true.
	 */
	public void setIgnoreDuplicates(boolean ignoreDuplicates)
	{
		this.ignoreDuplicates = ignoreDuplicates;
	}

	private List<OsmNode> nodes = new ArrayList<>();
	private List<OsmWay> ways = new ArrayList<>();
	private List<OsmRelation> relations = new ArrayList<>();

	private enum Mode {
		START,
		NODES,
		WAYS,
		RELATIONS,
		END
	}

	private Mode mode = Mode.START;
	private int index = -1;

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
		return input.hasBounds();
	}

	@Override
	public OsmBounds getBounds()
	{
		return input.getBounds();
	}

	@Override
	public boolean hasNext()
	{
		switch (mode) {
		default:
		case NODES:
			if (index < nodes.size()) {
				return true;
			} else {
				return !ways.isEmpty() || !relations.isEmpty()
						|| input.hasNext();
			}
		case WAYS:
			if (index < ways.size()) {
				return true;
			} else {
				return !relations.isEmpty() || input.hasNext();
			}
		case RELATIONS:
			return index < relations.size();
		case START:
			return input.hasNext();
		case END:
			return false;
		}
	}

	@Override
	public EntityContainer next()
	{
		EntityContainer container = nextOrSwitchMode();
		if (container != null) {
			return container;
		}
		return nextOrSwitchMode();
	}

	private EntityContainer nextOrSwitchMode()
	{
		switch (mode) {
		default:
		case NODES:
			if (index < nodes.size()) {
				OsmNode node = nodes.get(index++);
				skipDuplicates(nodes, node.getId());
				return new EntityContainer(EntityType.Node, node);
			} else {
				nodes.clear();
				setMode();
				read();
			}
			break;
		case WAYS:
			if (index < ways.size()) {
				OsmWay way = ways.get(index++);
				skipDuplicates(ways, way.getId());
				return new EntityContainer(EntityType.Way, way);
			} else {
				ways.clear();
				setMode();
				read();
			}
			break;
		case RELATIONS:
			if (index < relations.size()) {
				OsmRelation relation = relations.get(index++);
				skipDuplicates(relations, relation.getId());
				return new EntityContainer(EntityType.Relation, relation);
			} else {
				relations.clear();
				setMode();
			}
			break;
		case START:
			init();
			break;
		case END:
			throw new NoSuchElementException();
		}
		return null;
	}

	private void init()
	{
		EntityContainer container = input.next();
		switch (container.getType()) {
		default:
		case Node:
			nodes.add((OsmNode) container.getEntity());
			break;
		case Way:
			ways.add((OsmWay) container.getEntity());
			break;
		case Relation:
			relations.add((OsmRelation) container.getEntity());
			break;
		}
		setMode();
		read();
	}

	private void setMode()
	{
		if (!nodes.isEmpty()) {
			mode = Mode.NODES;
		} else if (!ways.isEmpty()) {
			mode = Mode.WAYS;
		} else if (!relations.isEmpty()) {
			mode = Mode.RELATIONS;
		} else {
			mode = Mode.END;
		}
	}

	private void read()
	{
		switch (mode) {
		case NODES:
			while (input.hasNext()) {
				EntityContainer container = input.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Node) {
					nodes.add((OsmNode) entity);
				} else if (container.getType() == EntityType.Way) {
					ways.add((OsmWay) entity);
				} else if (container.getType() == EntityType.Relation) {
					relations.add((OsmRelation) entity);
				}
			}
			Collections.sort(nodes, comparatorNodes);
			index = 0;
			break;
		case WAYS:
			while (input.hasNext()) {
				EntityContainer container = input.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Way) {
					ways.add((OsmWay) entity);
				} else if (container.getType() == EntityType.Relation) {
					relations.add((OsmRelation) entity);
				}
			}
			Collections.sort(ways, comparatorWays);
			index = 0;
			break;
		case RELATIONS:
			while (input.hasNext()) {
				EntityContainer container = input.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Relation) {
					relations.add((OsmRelation) entity);
				}
			}
			Collections.sort(relations, comparatorRelations);
			index = 0;
			break;
		default:
		case END:
		case START:
			break;
		}
	}

	private <T extends OsmEntity> void skipDuplicates(List<T> elements, long id)
	{
		while (index < elements.size()) {
			if (elements.get(index).getId() == id) {
				index++;
				continue;
			}
			break;
		}
	}

}

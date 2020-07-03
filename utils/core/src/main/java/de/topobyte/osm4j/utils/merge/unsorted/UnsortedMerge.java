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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class UnsortedMerge extends AbstractUnsortedMerge
{

	private OsmOutputStream output;

	/**
	 * Merge the elements from a collection of OSM input sources to a single OSM
	 * output. The merging algorithm expects the input data to be in default
	 * order, i.e. a sequence of nodes, followed by a sequence of ways, followed
	 * by a sequence of relations.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param inputs
	 *            a collection of iterators to retrieve data from.
	 */
	public UnsortedMerge(OsmOutputStream output, Collection<OsmIterator> inputs)
	{
		super(inputs);
		this.output = output;
	}

	public void run() throws IOException
	{
		// First initialize the input sources and put into the dequeues
		prepare();
		// Then iterate the sources as long as input is available
		iterate();
		// Finally, generate a complete() event on the output
		output.complete();
	}

	private void prepare() throws IOException
	{
		if (hasBounds) {
			output.write(bounds);
		}

		for (OsmIterator iterator : inputs) {
			if (!iterator.hasNext()) {
				continue;
			}
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
	}

	private void iterate() throws IOException
	{
		while (!nodeItems.isEmpty()) {
			Input<OsmNode> item = nodeItems.poll();

			output.write(item.firstEntity);
			item.firstEntity = null;
			OsmIterator iterator = item.iterator;

			while (iterator.hasNext()) {
				EntityContainer container = iterator.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Node) {
					output.write((OsmNode) entity);
				} else if (container.getType() == EntityType.Way) {
					wayItems.add(createItem((OsmWay) entity, iterator));
				} else if (container.getType() == EntityType.Relation) {
					relationItems
							.add(createItem((OsmRelation) entity, iterator));
				}
			}
		}

		while (!wayItems.isEmpty()) {
			Input<OsmWay> item = wayItems.poll();

			output.write(item.firstEntity);
			item.firstEntity = null;
			OsmIterator iterator = item.iterator;

			while (iterator.hasNext()) {
				EntityContainer container = iterator.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Way) {
					output.write((OsmWay) entity);
				} else if (container.getType() == EntityType.Relation) {
					relationItems
							.add(createItem((OsmRelation) entity, iterator));
				} else {
					break;
				}
			}
		}

		while (!relationItems.isEmpty()) {
			Input<OsmRelation> item = relationItems.poll();

			output.write(item.firstEntity);
			item.firstEntity = null;
			OsmIterator iterator = item.iterator;

			while (iterator.hasNext()) {
				EntityContainer container = iterator.next();
				OsmEntity entity = container.getEntity();
				if (container.getType() == EntityType.Relation) {
					output.write((OsmRelation) entity);
				} else {
					break;
				}
			}
		}
	}

}

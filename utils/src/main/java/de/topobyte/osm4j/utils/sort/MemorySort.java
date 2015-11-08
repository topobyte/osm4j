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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class MemorySort
{

	private OsmOutputStream output;
	private OsmIterator input;

	private boolean ignoreDuplicates = true;

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	/**
	 * Sort the elements from a OSM input source by their id in memory and write
	 * to an OSM output in that order. Sorting is applied to elements of the
	 * same type, i.e. the output contains first nodes, then ways and then
	 * relations, each list of elements ordered by the elements' ids.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 */
	public MemorySort(OsmOutputStream output, OsmIterator input)
	{
		this(output, input, new IdComparator());
	}

	/**
	 * Sort the elements from a OSM input source using the supplied comparator
	 * in memory and write to an OSM output in that order. Sorting is applied to
	 * elements of the same type, i.e. the output contains first nodes, then
	 * ways and then relations, each list of elements sorted using the
	 * comparator.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 * @param comparator
	 *            a Comparator used to sort each list of elements.
	 */
	public MemorySort(OsmOutputStream output, OsmIterator input,
			Comparator<OsmEntity> comparator)
	{
		this(output, input, comparator, comparator, comparator);
	}

	/**
	 * Sort the elements from a OSM input source using the supplied comparators
	 * in memory and write to an OSM output in that order. Sorting is applied to
	 * elements of the same type, i.e. the output contains first nodes, then
	 * ways and then relations, each list of elements sorted using the
	 * respective comparator.
	 * 
	 * @param output
	 *            am OsmOutputStream to write data to.
	 * @param input
	 *            an OsmIterator to retrieve data from.
	 * @param comparatorNodes
	 *            a Comparator used to sort the list of nodes.
	 * @param comparatorWays
	 *            a Comparator used to sort the list of ways.
	 * @param comparatorRelations
	 *            a Comparator used to sort the list of relations.
	 */
	public MemorySort(OsmOutputStream output, OsmIterator input,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
	{
		this.output = output;
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

	public void run() throws IOException
	{
		execute();
		output.complete();
	}

	private boolean hasMore = true;
	private EntityContainer next = null;

	private void advance()
	{
		hasMore = input.hasNext();
		if (hasMore) {
			next = input.next();
		} else {
			next = null;
		}
	}

	private void execute() throws IOException
	{
		if (input.hasBounds()) {
			output.write(input.getBounds());
		}

		// read first element
		advance();

		// collect all nodes
		while (hasMore && next.getType() == EntityType.Node) {
			nodes.add((OsmNode) next.getEntity());
			advance();
		}
		// sort
		Collections.sort(nodes, comparatorNodes);
		// write
		writeNodes();

		// release node resources
		nodes.clear();

		// collect all ways
		while (hasMore && next.getType() == EntityType.Way) {
			ways.add((OsmWay) next.getEntity());
			advance();
		}
		// sort
		Collections.sort(ways, comparatorWays);
		// write
		writeWays();

		// release way resources
		ways.clear();

		// collect all relations
		while (hasMore && next.getType() == EntityType.Relation) {
			relations.add((OsmRelation) next.getEntity());
			advance();
		}
		// sort
		Collections.sort(relations, comparatorRelations);
		// write
		writeRelations();

		// release relation resources
		relations.clear();
	}

	private void writeNodes() throws IOException
	{
		EntityWriter<OsmNode> writer = new EntityWriter<OsmNode>() {

			@Override
			public void write(OsmNode node) throws IOException
			{
				output.write(node);
			}
		};
		writer.write(nodes);
	}

	private void writeWays() throws IOException
	{
		EntityWriter<OsmWay> writer = new EntityWriter<OsmWay>() {

			@Override
			public void write(OsmWay way) throws IOException
			{
				output.write(way);
			}
		};
		writer.write(ways);
	}

	private void writeRelations() throws IOException
	{
		EntityWriter<OsmRelation> writer = new EntityWriter<OsmRelation>() {

			@Override
			public void write(OsmRelation relation) throws IOException
			{
				output.write(relation);
			}
		};
		writer.write(relations);
	}

	private abstract class EntityWriter<T extends OsmEntity>
	{

		public abstract void write(T entity) throws IOException;

		public void write(List<T> elements) throws IOException
		{
			if (!ignoreDuplicates) {
				writeAll(elements);
			} else {
				writeUnique(elements);
			}
		}

		public void writeAll(List<T> elements) throws IOException
		{
			for (T element : elements) {
				write(element);
			}
		}

		private void writeUnique(List<T> elements) throws IOException
		{
			if (elements.isEmpty()) {
				return;
			}
			T element = elements.get(0);
			write(element);
			long last = element.getId();

			for (int i = 1; i < elements.size(); i++) {
				element = elements.get(i);
				if (last == element.getId()) {
					continue;
				}
				write(element);
				last = element.getId();
			}
		}

	}

}

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

package de.topobyte.osm4j.extra.relations;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.topobyte.adt.graph.Graph;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;

public class RelationGraph
{

	private Graph<Long> graph = new Graph<>();

	// ids of relations that have relation members
	private TLongSet idsHasChildRelations = new TLongHashSet();
	// ids of relations are members of other relations
	private TLongSet idsIsChildRelation = new TLongHashSet();
	// ids of relations that neither have relation members nor are member of any
	// other relation
	private TLongSet idsSimpleRelations = new TLongHashSet();
	// number of relations that do not have relation members
	private int numNoChildren = 0;

	public void build(OsmIterator iterator, boolean storeSimpleRelations)
			throws IOException
	{
		for (EntityContainer container : iterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			process(relation, storeSimpleRelations);
		}
	}

	public void build(InMemoryDataSet data, boolean storeSimpleRelations)
			throws IOException
	{
		for (OsmRelation relation : data.getRelations().valueCollection()) {
			process(relation, storeSimpleRelations);
		}
	}

	private void process(OsmRelation relation, boolean storeSimpleRelations)
	{
		boolean hasChildRelations = false;
		TLongList childRelationMembers = new TLongArrayList();
		for (OsmRelationMember member : OsmModelUtil.membersAsList(relation)) {
			if (member.getType() == EntityType.Relation) {
				hasChildRelations = true;
				idsIsChildRelation.add(member.getId());
				childRelationMembers.add(member.getId());
				if (storeSimpleRelations) {
					idsSimpleRelations.remove(member.getId());
				}
			}
		}
		long id = relation.getId();
		if (hasChildRelations) {
			idsHasChildRelations.add(id);
			graph.addNode(id);

			TLongIterator iterator = childRelationMembers.iterator();
			while (iterator.hasNext()) {
				long member = iterator.next();
				if (!graph.getNodes().contains(member)) {
					graph.addNode(member);
				}
				graph.addEdge(id, member);
			}
		} else {
			if (storeSimpleRelations && !idsIsChildRelation.contains(id)) {
				idsSimpleRelations.add(id);
			}
			numNoChildren++;
		}
	}

	public int getNumNoChildren()
	{
		return numNoChildren;
	}

	public Graph<Long> getGraph()
	{
		return graph;
	}

	public TLongSet getIdsHasChildRelations()
	{
		return idsHasChildRelations;
	}

	public TLongSet getIdsIsChildRelation()
	{
		return idsIsChildRelation;
	}

	public TLongSet getIdsSimpleRelations()
	{
		return idsSimpleRelations;
	}

	public List<Group> buildGroups()
	{
		List<Group> groups = new LinkedList<>();

		TLongSet starts = new TLongHashSet();
		Collection<Long> ids = graph.getNodes();
		for (long id : ids) {
			if (graph.getEdgesIn(id).isEmpty()) {
				starts.add(id);
			}
		}
		System.out.println("Number of start relations: " + starts.size());
		for (long start : starts.toArray()) {
			groups.add(build(start));
		}

		return groups;
	}

	private Group build(long start)
	{
		TLongSet group = new TLongHashSet();
		group.add(start);

		TLongSet left = new TLongHashSet();
		left.addAll(graph.getEdgesOut(start));

		while (!left.isEmpty()) {
			TLongIterator iterator = left.iterator();
			long next = iterator.next();
			iterator.remove();

			if (group.contains(next)) {
				continue;
			}
			group.add(next);
			Set<Long> out = graph.getEdgesOut(next);
			left.addAll(out);
		}

		return new Group(start, group);
	}

}

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
import de.topobyte.adt.graph.UndirectedGraph;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.util.IdUtil;

public class RelationGraph
{

	private Graph<Long> graph;

	// ids of relations that have relation members
	private TLongSet idsHasChildRelations = new TLongHashSet();
	// ids of relations are members of other relations
	private TLongSet idsIsChildRelation = new TLongHashSet();
	// ids of relations that neither have relation members nor are member of any
	// other relation
	private TLongSet idsSimpleRelations = new TLongHashSet();
	// number of relations that do not have relation members
	private int numNoChildren = 0;

	private boolean storeSimpleRelations;
	private boolean undirected;

	public RelationGraph(boolean storeSimpleRelations, boolean undirected)
	{
		this.storeSimpleRelations = storeSimpleRelations;
		this.undirected = undirected;
	}

	public void build(OsmIterator iterator) throws IOException
	{
		graph = undirected ? new UndirectedGraph<Long>() : new Graph<Long>();
		for (EntityContainer container : iterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			process(relation);
		}
	}

	public void build(Collection<OsmRelation> relations) throws IOException
	{
		graph = undirected ? new UndirectedGraph<Long>() : new Graph<Long>();
		for (OsmRelation relation : relations) {
			process(relation);
		}
	}

	private void process(OsmRelation relation)
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
			if (!graph.getNodes().contains(id)) {
				graph.addNode(id);
			}

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
		if (undirected) {
			return buildGroupsUndirected();
		} else {
			return buildGroupsDirected();
		}
	}

	public List<Group> buildGroupsUndirected()
	{
		List<Group> groups = new LinkedList<>();

		TLongSet nodes = new TLongHashSet(graph.getNodes());
		while (!nodes.isEmpty()) {
			long id = any(nodes);

			TLongSet reachable = reachable(graph, id);
			nodes.removeAll(reachable);

			groups.add(new Group(id, reachable));
		}
		return groups;
	}

	private TLongSet reachable(Graph<Long> graph, long id)
	{
		TLongSet reached = new TLongHashSet();
		TLongSet queue = new TLongHashSet();

		queue.add(id);

		while (!queue.isEmpty()) {
			long current = any(queue);
			reached.add(current);

			Set<Long> out = graph.getEdgesOut(current);
			for (long next : out) {
				if (!reached.contains(next)) {
					queue.add(next);
				}
			}
		}

		return reached;
	}

	private long any(TLongSet nodes)
	{
		TLongIterator iterator = nodes.iterator();
		long id = iterator.next();
		iterator.remove();
		return id;
	}

	public List<Group> buildGroupsDirected()
	{
		List<Group> groups = new LinkedList<>();

		// First determine 'start' relations, i.e. relations without incoming
		// edges in the relation graph
		TLongSet starts = new TLongHashSet();
		Collection<Long> ids = graph.getNodes();
		for (long id : ids) {
			if (graph.getEdgesIn(id).isEmpty()) {
				starts.add(id);
			}
		}
		// Build sub-graphs reachable from 'start' relations
		System.out.println("Number of start relations: " + starts.size());
		for (long start : starts.toArray()) {
			groups.add(build(start));
		}

		// In case of circles within the relation graph that are not reachable
		// from any start relation, there may be some relations left, that have
		// not been put into groups yet.
		TLongSet remaining = new TLongHashSet();
		remaining.addAll(ids);
		for (Group group : groups) {
			remaining.removeAll(group.getRelationIds());
		}
		if (remaining.size() > 0) {
			System.out.println("remaining: " + remaining.size());
			while (!remaining.isEmpty()) {
				long id = any(remaining);

				TLongSet reachable = reachable(graph, id);
				remaining.removeAll(reachable);

				long lowest = IdUtil.lowestId(reachable);
				groups.add(new Group(lowest, reachable));
			}
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
			long next = any(left);

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

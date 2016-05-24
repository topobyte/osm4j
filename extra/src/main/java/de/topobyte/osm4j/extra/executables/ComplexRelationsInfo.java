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

package de.topobyte.osm4j.extra.executables;

import gnu.trove.set.TLongSet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;

public class ComplexRelationsInfo extends AbstractExecutableSingleInputStream
{

	@Override
	protected String getHelpMessage()
	{
		return ComplexRelationsInfo.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException,
			EntityNotFoundException
	{
		ComplexRelationsInfo task = new ComplexRelationsInfo();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private void execute() throws IOException, EntityNotFoundException
	{
		RelationGraph graph = new RelationGraph(true, true);

		OsmIterator iterator = createIterator();
		InMemoryListDataSet data = ListDataSetLoader.read(iterator, true, true,
				true);
		data.sort();
		graph.build(data.getRelations());

		System.out
				.println("Number of relations: " + data.getRelations().size());

		TLongSet simpleRelationIds = graph.getIdsSimpleRelations();
		System.out.println("Number of simple relations: "
				+ simpleRelationIds.size());

		EntityFinder finder = EntityFinders.create(data,
				EntityNotFoundStrategy.LOG_WARN);

		long n = countWayReferences(data.getRelations());
		long m = 0;
		long l = 0;

		List<Group> groups = graph.buildGroups();
		System.out.println("Number of complex groups: " + groups.size());
		for (Group group : groups) {
			TLongSet ids = group.getRelationIds();
			System.out.println(String.format("Group with %d relations",
					ids.size()));

			List<OsmRelation> groupRelations = finder.findRelations(ids);
			RelationGraph groupGraph = new RelationGraph(true, false);
			groupGraph.build(groupRelations);
			List<Group> groupGroups = groupGraph.buildGroups();
			System.out.println("Number of subgroups: " + groupGroups.size());

			for (Group subGroup : groupGroups) {
				l += subGroup.getNumRelations();
				List<OsmRelation> subRelations = finder.findRelations(subGroup
						.getRelationIds());
				m += countWayReferences(subRelations);
			}
		}

		System.out.println("Number of relations in subgroups: " + l);
		System.out.println("Number of referenced ways: " + n);
		System.out.println("Number of referenced ways by subgroups: " + m);
	}

	private long countWayReferences(Collection<OsmRelation> relations)
	{
		long n = 0;
		for (OsmRelation relation : relations) {
			for (OsmRelationMember member : OsmModelUtil
					.membersAsList(relation)) {
				if (member.getType() != EntityType.Way) {
					continue;
				}
				n++;
			}
		}
		return n;
	}

}

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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.slimjars.dist.gnu.trove.set.TLongSet;

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

	private static final String OPTION_GROUP_INFO = "groupinfo";
	private static final String OPTION_SUBGROUP_INFO = "subgroupinfo";

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

	private boolean groupInfo;
	private boolean subGroupInfo;

	public ComplexRelationsInfo()
	{
		options.addOption(OPTION_GROUP_INFO, false,
				"print detailes about groups");
		options.addOption(OPTION_SUBGROUP_INFO, false,
				"print details about subgroups");
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		groupInfo = line.hasOption(OPTION_GROUP_INFO);
		subGroupInfo = line.hasOption(OPTION_SUBGROUP_INFO);
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
		System.out.println("Size of complex relation graph: "
				+ graph.getGraph().getNodes().size());
		System.out.println("Complex relation graph info:");
		System.out.println("  Number of relations with children: "
				+ graph.getIdsHasChildRelations().size());
		System.out.println("  Number of relations without children: "
				+ (graph.getNumNoChildren() - simpleRelationIds.size()));
		System.out.println("  Number of child relations: "
				+ graph.getIdsIsChildRelation().size());

		EntityFinder finder = EntityFinders.create(data,
				EntityNotFoundStrategy.LOG_WARN);

		long n = countWayReferences(data.getRelations());
		long m = 0;
		long l = 0;

		List<Group> groups = graph.buildGroups();
		Collections.sort(groups, new GroupComparator());

		System.out.println("Number of complex groups: " + groups.size());
		for (Group group : groups) {
			TLongSet ids = group.getRelationIds();

			List<OsmRelation> groupRelations = finder.findRelations(ids);
			RelationGraph groupGraph = new RelationGraph(true, false);
			groupGraph.build(groupRelations);
			List<Group> groupGroups = groupGraph.buildGroups();
			if (groupInfo || subGroupInfo) {
				System.out.println(String.format(
						"Group start=%d has %d relations and %d subgroups",
						group.getStart(), ids.size(), groupGroups.size()));
			}

			Collections.sort(groupGroups, new GroupComparator());

			for (Group subGroup : groupGroups) {
				if (subGroupInfo && groupGroups.size() > 1) {
					System.out.println(String.format(
							"  Subgroup start=%d has %d relations", subGroup
									.getStart(), subGroup.getRelationIds()
									.size()));
				}

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

	private static class GroupComparator implements Comparator<Group>
	{

		@Override
		public int compare(Group o1, Group o2)
		{
			return Long.compare(o1.getStart(), o2.getStart());
		}

	}

}

// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.extracts.query;

import gnu.trove.set.TLongSet;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.jts.utils.GeometryGroup;
import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import de.topobyte.osm4j.geometry.BboxBuilder;
import de.topobyte.osm4j.geometry.LineworkBuilderResult;
import de.topobyte.osm4j.geometry.RegionBuilderResult;

public class ComplexRelationsQuery extends AbstractRelationsQuery
{

	public ComplexRelationsQuery(InMemoryListDataSet dataNodes,
			InMemoryListDataSet dataWays, InMemoryListDataSet dataRelations,
			PredicateEvaluator test, boolean fastRelationTests)
	{
		super(dataNodes, dataWays, dataRelations, test, fastRelationTests);
	}

	public void execute(RelationQueryBag queryBag) throws IOException
	{
		EntityFinder finder = EntityFinders.create(dataRelations,
				EntityNotFoundStrategy.IGNORE);

		Set<OsmRelation> found = new HashSet<>();

		RelationGraph relationGraph = new RelationGraph(false, true);
		relationGraph.build(dataRelations.getRelations());
		List<Group> groups = relationGraph.buildGroups();
		for (Group group : groups) {
			TLongSet ids = group.getRelationIds();
			System.out.println(String.format("group with %d relations",
					ids.size()));

			List<OsmRelation> groupRelations;
			try {
				groupRelations = finder.findRelations(ids);
			} catch (EntityNotFoundException e) {
				// Can't happen, using the IGNORE strategy
				continue;
			}
			RelationGraph groupGraph = new RelationGraph(true, false);
			groupGraph.build(groupRelations);
			List<Group> groupGroups = groupGraph.buildGroups();
			System.out.println("subgroups: " + groupGroups.size());

			for (Group subGroup : groupGroups) {
				OsmRelation start;
				List<OsmRelation> subRelations;
				try {
					start = dataRelations.getRelation(group.getStart());
					subRelations = finder.findRelations(subGroup
							.getRelationIds());
				} catch (EntityNotFoundException e) {
					// Can't happen, using the IGNORE strategy
					continue;
				}
				if (intersects(start, subRelations, queryBag)) {
					found.addAll(subRelations);
				}
			}
		}

		queryBag.nComplex += found.size();

		for (OsmRelation relation : found) {
			queryBag.outRelations.getOsmOutput().write(relation);
		}
		for (OsmRelation relation : found) {
			try {
				QueryUtil.putNodes(relation, queryBag.additionalNodes,
						dataNodes, queryBag.nodeIds);
				QueryUtil.putWaysAndWayNodes(relation,
						queryBag.additionalNodes, queryBag.additionalWays,
						provider, queryBag.nodeIds, queryBag.wayIds);
			} catch (EntityNotFoundException e) {
				System.out.println("Unable to find all members for relation: "
						+ relation.getId());
			}
		}
	}

	private boolean intersects(OsmRelation start, List<OsmRelation> relations,
			RelationQueryBag queryBag) throws IOException
	{
		EntityFinder finder = EntityFinders.create(dataRelations,
				EntityNotFoundStrategy.IGNORE);

		boolean in = QueryUtil.anyMemberContainedIn(relations,
				queryBag.nodeIds, queryBag.wayIds);

		if (!in && fastRelationTests) {
			Set<OsmNode> nodes = new HashSet<>();
			try {
				finder.findMemberNodesAndWayNodes(relations, nodes);
			} catch (EntityNotFoundException e) {
				// Can't happen, because we're using the IGNORE strategy
			}

			Envelope envelope = BboxBuilder.box(nodes);
			if (test.intersects(envelope)) {
				in = true;
			}
		}

		if (!in && !fastRelationTests) {
			try {
				LineworkBuilderResult result = lineworkBuilder.build(relations,
						provider);
				GeometryGroup group = result.toGeometryGroup(factory);
				if (test.intersects(group)) {
					in = true;
				}
			} catch (EntityNotFoundException e) {
				System.out.println("Unable to build relation group");
			}
		}

		if (!in && !fastRelationTests) {
			try {
				RegionBuilderResult result = regionBuilder.build(start,
						provider);
				GeometryGroup group = result.toGeometryGroup(factory);
				if (test.intersects(group)) {
					in = true;
				}
			} catch (EntityNotFoundException e) {
				System.out.println("Unable to build relation group");
			}
		}

		return in;
	}

}

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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.extra.MissingEntityCounter;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;

public class ComplexRelationsQuery extends AbstractRelationsQuery
{

	final static Logger logger = LoggerFactory
			.getLogger(ComplexRelationsQuery.class);

	public ComplexRelationsQuery(InMemoryListDataSet dataNodes,
			InMemoryListDataSet dataWays, InMemoryListDataSet dataRelations,
			PredicateEvaluator test, boolean fastRelationTests)
	{
		super(dataNodes, dataWays, dataRelations, test, fastRelationTests);
	}

	public void execute(RelationQueryBag queryBag) throws IOException
	{
		RelationGraph relationGraph = new RelationGraph(true, true);
		relationGraph.build(dataRelations.getRelations());
		List<Group> groups = relationGraph.buildGroups();
		logger.debug(String.format(
				"This batch has %d groups and %d simple relations",
				groups.size(), relationGraph.getIdsSimpleRelations().size()));

		Set<OsmRelation> foundSimple = new HashSet<>();
		Set<OsmRelation> foundComplex = new HashSet<>();

		if (!relationGraph.getIdsSimpleRelations().isEmpty()) {
			executeSimple(queryBag, relationGraph.getIdsSimpleRelations(),
					foundSimple);
		}

		if (!groups.isEmpty()) {
			executeGroups(queryBag, groups, foundComplex);
		}

		SetView<OsmRelation> found = Sets.union(foundSimple, foundComplex);

		TLongObjectMap<OsmRelation> relations = new TLongObjectHashMap<>();
		for (OsmRelation relation : found) {
			relations.put(relation.getId(), relation);
		}
		logger.debug(String.format("writing %d relations", relations.size()));
		QueryUtil.writeRelations(relations,
				queryBag.outRelations.getOsmOutput());
	}

	private void executeSimple(RelationQueryBag queryBag,
			TLongSet simpleRelationIds, Set<OsmRelation> found)
			throws IOException
	{
		EntityFinder finder = EntityFinders.create(provider,
				EntityNotFoundStrategy.IGNORE);

		for (OsmRelation relation : dataRelations.getRelations()) {
			if (!simpleRelationIds.contains(relation.getId())) {
				continue;
			}

			if (!intersects(relation, queryBag, finder)) {
				continue;
			}

			found.add(relation);
			queryBag.nSimple++;

			MissingEntityCounter counter = new MissingEntityCounter();
			QueryUtil.putNodes(relation, queryBag.additionalNodes, dataNodes,
					queryBag.nodeIds, counter);
			QueryUtil
					.putWaysAndWayNodes(relation, queryBag.additionalNodes,
							queryBag.additionalWays, provider, queryBag.wayIds,
							counter);

			if (counter.nonZero()) {
				System.out.println(String.format(
						"relation %d: unable to find %s", relation.getId(),
						counter.toMessage()));
			}
		}
	}

	private void executeGroups(RelationQueryBag queryBag, List<Group> groups,
			Set<OsmRelation> found) throws IOException
	{
		EntityFinder finder = EntityFinders.create(provider,
				EntityNotFoundStrategy.IGNORE);

		for (Group group : groups) {
			TLongSet ids = group.getRelationIds();
			logger.debug(String.format("group with %d relations", ids.size()));

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
			logger.debug("subgroups: " + groupGroups.size());

			for (Group subGroup : groupGroups) {
				OsmRelation start;
				List<OsmRelation> subRelations;
				try {
					start = dataRelations.getRelation(subGroup.getStart());
					subRelations = finder.findRelations(subGroup
							.getRelationIds());
				} catch (EntityNotFoundException e) {
					// Can't happen, using the IGNORE strategy
					continue;
				}
				if (intersects(start, subRelations, queryBag, finder)) {
					found.addAll(subRelations);
				}
			}
		}

		queryBag.nComplex += found.size();

		for (OsmRelation relation : found) {
			MissingEntityCounter counter = new MissingEntityCounter();
			QueryUtil.putNodes(relation, queryBag.additionalNodes, dataNodes,
					queryBag.nodeIds, counter);
			QueryUtil
					.putWaysAndWayNodes(relation, queryBag.additionalNodes,
							queryBag.additionalWays, provider, queryBag.wayIds,
							counter);

			if (counter.nonZero()) {
				System.out.println(String.format(
						"relation %d: unable to find %s", relation.getId(),
						counter.toMessage()));
			}
		}
	}

}

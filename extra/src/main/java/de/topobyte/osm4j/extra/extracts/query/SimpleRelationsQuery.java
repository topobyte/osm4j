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
import de.topobyte.osm4j.geometry.BboxBuilder;
import de.topobyte.osm4j.geometry.LineworkBuilderResult;
import de.topobyte.osm4j.geometry.RegionBuilderResult;

public class SimpleRelationsQuery extends AbstractRelationsQuery
{

	public SimpleRelationsQuery(InMemoryListDataSet dataNodes,
			InMemoryListDataSet dataWays, InMemoryListDataSet dataRelations,
			PredicateEvaluator test, boolean fastRelationTests)
	{
		super(dataNodes, dataWays, dataRelations, test, fastRelationTests);
	}

	public void execute(RelationQueryBag queryBag) throws IOException
	{
		EntityFinder finder = EntityFinders.create(provider,
				EntityNotFoundStrategy.IGNORE);

		for (OsmRelation relation : dataRelations.getRelations()) {
			if (!intersects(relation, queryBag, finder)) {
				continue;
			}

			queryBag.outRelations.getOsmOutput().write(relation);
			queryBag.nSimple++;
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

	private boolean intersects(OsmRelation relation, RelationQueryBag queryBag,
			EntityFinder finder)
	{
		if (QueryUtil.anyMemberContainedIn(relation, queryBag.nodeIds,
				queryBag.wayIds)) {
			return true;
		}

		Set<OsmNode> nodes = new HashSet<>();
		try {
			finder.findMemberNodesAndWayNodes(relation, nodes);
		} catch (EntityNotFoundException e) {
			// Can't happen, because we're using the IGNORE strategy
		}

		Envelope envelope = BboxBuilder.box(nodes);
		if (test.intersects(envelope)) {
			if (fastRelationTests) {
				return true;
			}
		} else {
			return false;
		}

		try {
			LineworkBuilderResult result = lineworkBuilder.build(relation,
					provider);
			GeometryGroup group = result.toGeometryGroup(factory);
			if (test.intersects(group)) {
				return true;
			}
		} catch (EntityNotFoundException e) {
			System.out.println("Unable to build relation: " + relation.getId());
		}

		try {
			RegionBuilderResult result = regionBuilder
					.build(relation, provider);
			GeometryGroup group = result.toGeometryGroup(factory);
			if (test.intersects(group)) {
				return true;
			}
		} catch (EntityNotFoundException e) {
			System.out.println("Unable to build relation: " + relation.getId());
		}

		return false;
	}

}

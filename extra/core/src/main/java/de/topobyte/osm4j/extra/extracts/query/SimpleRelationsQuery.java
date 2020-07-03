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

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.extra.MissingEntityCounter;
import de.topobyte.osm4j.extra.QueryUtil;

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

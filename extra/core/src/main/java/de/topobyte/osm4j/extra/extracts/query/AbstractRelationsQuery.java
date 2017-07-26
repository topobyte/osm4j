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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.GeometryGroup;
import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.CompositeOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.geometry.BboxBuilder;
import de.topobyte.osm4j.geometry.LineworkBuilder;
import de.topobyte.osm4j.geometry.LineworkBuilderResult;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import de.topobyte.osm4j.geometry.MissingWayNodeStrategy;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;

public abstract class AbstractRelationsQuery
{

	protected InMemoryListDataSet dataNodes;
	protected InMemoryListDataSet dataWays;
	protected InMemoryListDataSet dataRelations;

	protected PredicateEvaluator test;

	protected boolean fastRelationTests;

	protected CompositeOsmEntityProvider provider;

	protected GeometryFactory factory = new GeometryFactory();
	protected LineworkBuilder lineworkBuilder = new LineworkBuilder(factory);
	protected RegionBuilder regionBuilder = new RegionBuilder(factory);

	public AbstractRelationsQuery(InMemoryListDataSet dataNodes,
			InMemoryListDataSet dataWays, InMemoryListDataSet dataRelations,
			PredicateEvaluator test, boolean fastRelationTests)
	{
		this.dataNodes = dataNodes;
		this.dataWays = dataWays;
		this.dataRelations = dataRelations;
		this.test = test;
		this.fastRelationTests = fastRelationTests;

		provider = new CompositeOsmEntityProvider(dataNodes, dataWays,
				dataRelations);

		lineworkBuilder
				.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
		lineworkBuilder
				.setMissingWayNodeStrategy(MissingWayNodeStrategy.SPLIT_POLYLINE);
		regionBuilder
				.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
		regionBuilder
				.setMissingWayNodeStrategy(MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE);
	}

	protected boolean intersects(OsmRelation relation,
			RelationQueryBag queryBag, EntityFinder finder)
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

	protected boolean intersects(OsmRelation start,
			List<OsmRelation> relations, RelationQueryBag queryBag,
			EntityFinder finder) throws IOException
	{
		if (QueryUtil.anyMemberContainedIn(relations, queryBag.nodeIds,
				queryBag.wayIds)) {
			return true;
		}

		Set<OsmNode> nodes = new HashSet<>();
		try {
			finder.findMemberNodesAndWayNodes(relations, nodes);
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
			LineworkBuilderResult result = lineworkBuilder.build(relations,
					provider);
			GeometryGroup group = result.toGeometryGroup(factory);
			if (test.intersects(group)) {
				return true;
			}
		} catch (EntityNotFoundException e) {
			System.out.println("Unable to build relation group");
		}

		try {
			RegionBuilderResult result = regionBuilder.build(start, provider);
			GeometryGroup group = result.toGeometryGroup(factory);
			if (test.intersects(group)) {
				return true;
			}
		} catch (EntityNotFoundException e) {
			System.out.println("Unable to build relation group");
		}

		return false;
	}

}

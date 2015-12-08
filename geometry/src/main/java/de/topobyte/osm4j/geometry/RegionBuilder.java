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

package de.topobyte.osm4j.geometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.topobyte.adt.multicollections.CountingMultiValMap;
import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.jts.utils.PolygonHelper;
import de.topobyte.jts.utils.SelfIntersectionUtil;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.relation.RelationUtil;
import de.topobyte.osm4j.geometry.relation.SegmentRing;
import de.topobyte.osm4j.geometry.relation.WayRing;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class RegionBuilder
{

	final static Logger logger = LoggerFactory.getLogger(RegionBuilder.class);

	private GeometryFactory factory;

	private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
	private boolean log = false;
	private LogLevel logLevel = LogLevel.WARN;

	public RegionBuilder()
	{
		this(new GeometryFactory());
	}

	public RegionBuilder(GeometryFactory factory)
	{
		this.factory = factory;
	}

	public boolean isLog()
	{
		return log;
	}

	public void setLog(boolean log)
	{
		this.log = log;
	}

	public LogLevel getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel)
	{
		this.logLevel = logLevel;
	}

	public MissingEntitiesStrategy getMissingEntitiesStrategy()
	{
		return missingEntitiesStrategy;
	}

	public void setMissingEntitesStrategy(
			MissingEntitiesStrategy missingEntitesStrategy)
	{
		this.missingEntitiesStrategy = missingEntitesStrategy;
	}

	/**
	 * Build a MultiPolygon from a Relation.
	 * 
	 * @param relation
	 *            a relation to construct the region for.
	 * @return the constructed MultiPolygon.
	 * @throws EntityNotFoundException
	 *             when a required entity cannot be obtained.
	 */
	public MultiPolygon build(OsmRelation relation, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		EntityNotFoundStrategy enfs = Util.strategy(missingEntitiesStrategy,
				log, logLevel);

		logger.debug("building region id:" + relation.getId());
		Set<OsmRelation> relations = new HashSet<>();
		MultiSet<OsmWay> ways = new HashMultiSet<>();
		EntityFinder finder = EntityFinders.create(resolver, enfs);
		relations.add(relation);
		finder.findMemberRelations(relation, relations);
		finder.findMemberWays(relations, ways);
		return build(ways, resolver);
	}

	public MultiPolygon build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		MultiSet<OsmWay> ways = new HashMultiSet<>();
		ways.put(way);
		return build(ways, resolver);
	}

	/**
	 * Build a MultiPolygon from a Set of Ways.
	 * 
	 * @param ways
	 *            the ways to use for geometry construction.
	 * @return the constructed MultiPolygon.
	 * @throws EntityNotFoundException
	 *             when a required entity cannot be obtained.
	 */
	public MultiPolygon build(MultiSet<OsmWay> ways, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		logger.debug("Have " + ways.keySet().size() + " ways");

		CountingMultiValMap<Long, OsmWay> wayTailMap = RelationUtil
				.buildWayTailMap(ways);

		List<WayRing> rings = RelationUtil.buildRings(ways, wayTailMap);
		List<SegmentRing> segmentRings = RelationUtil
				.convertToSegmentRings(rings);

		segmentRings = RelationUtil.fixNodeIntersections(segmentRings);

		RelationUtil.checkRings(rings, resolver);

		// RelationUtil.closeUnclosedRingWithStraightLine(rings);

		MultiPolygon mp = buildMultipolygon(segmentRings, resolver);

		return mp;
	}

	private MultiPolygon buildMultipolygon(Collection<SegmentRing> rings,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		Set<LinearRing> linearRings = RelationUtil.toLinearRings(rings,
				resolver);

		Set<LinearRing> validRings = new HashSet<>();
		for (LinearRing r : linearRings) {
			Set<LinearRing> repaired = SelfIntersectionUtil.repair(r);
			for (LinearRing ring : repaired) {
				validRings.add(ring);
			}
		}

		return PolygonHelper.multipolygonFromRings(validRings, false);
	}

}

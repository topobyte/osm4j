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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;

import de.topobyte.adt.multicollections.CountingMultiValMap;
import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.jts.utils.PolygonHelper;
import de.topobyte.jts.utils.SelfIntersectionUtil;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.relation.ChainOfNodes;
import de.topobyte.osm4j.geometry.relation.ChainOfWays;
import de.topobyte.osm4j.geometry.relation.RelationUtil;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class RegionBuilder
{

	final static Logger logger = LoggerFactory.getLogger(RegionBuilder.class);

	private GeometryFactory factory;
	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;

	private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
	private boolean includePuntal = true;
	private boolean includeLineal = true;
	private boolean log = false;
	private LogLevel logLevel = LogLevel.WARN;

	public RegionBuilder()
	{
		this(new GeometryFactory());
	}

	public RegionBuilder(GeometryFactory factory)
	{
		this.factory = factory;
		nodeBuilder = new NodeBuilder(factory);
		wayBuilder = new WayBuilder(factory);
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

	public boolean isIncludePuntal()
	{
		return includePuntal;
	}

	public void setIncludePuntal(boolean includePuntal)
	{
		this.includePuntal = includePuntal;
	}

	public boolean isIncludeLineal()
	{
		return includeLineal;
	}

	public void setIncludeLineal(boolean includeLineal)
	{
		this.includeLineal = includeLineal;
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
	public Geometry build(OsmRelation relation, OsmEntityProvider resolver)
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

		Set<OsmNode> nodes = new HashSet<>();
		if (includePuntal) {
			finder.findMemberNodes(relations, nodes);
		}
		return build(ways, resolver, nodes);
	}

	public Geometry build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		MultiSet<OsmWay> ways = new HashMultiSet<>();
		ways.add(way);
		return build(ways, resolver, new HashSet<OsmNode>());
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
	public Geometry build(MultiSet<OsmWay> ways, OsmEntityProvider resolver,
			Set<OsmNode> nodes) throws EntityNotFoundException
	{
		logger.debug("Have " + ways.keySet().size() + " ways");

		// Only keep ways with 2 or more nodes. Also don't keep ways with just 2
		// times the same node.
		List<OsmWay> invalidWays = new ArrayList<>();
		for (OsmWay way : ways.keySet()) {
			int numNodes = way.getNumberOfNodes();
			if (numNodes == 0 || numNodes == 1) {
				invalidWays.add(way);
			} else if (numNodes == 2 && OsmModelUtil.isClosed(way)) {
				invalidWays.add(way);
			}
		}
		ways.removeAllOccurences(invalidWays);
		logger.debug("Removed " + invalidWays.size() + " invalid ways");

		CountingMultiValMap<Long, OsmWay> wayTailMap = RelationUtil
				.buildWayTailMap(ways);

		List<ChainOfWays> chains = RelationUtil.buildRings(ways, wayTailMap);

		// Chains that are closed and have enough nodes
		List<ChainOfWays> ringChains = new ArrayList<>();
		// Chains that are not closed or do not have enough nodes
		List<ChainOfWays> nonRingChains = new ArrayList<>();

		for (ChainOfWays chain : chains) {
			if (chain.isValidRing()) {
				ringChains.add(chain);
			} else {
				nonRingChains.add(chain);
			}
		}
		logger.debug("Number of ring chains: " + ringChains.size());
		logger.debug("Number of non-ring chains: " + nonRingChains.size());

		List<ChainOfNodes> rings = new ArrayList<>();
		List<ChainOfNodes> nonRings = new ArrayList<>();

		RelationUtil.convertToSegmentChainsAndResolveNodeIntersections(
				ringChains, rings, rings, nonRings);
		RelationUtil.convertToSegmentChainsAndResolveNodeIntersections(
				nonRingChains, nonRings, rings, nonRings);

		try {
			RelationUtil.checkRings(nonRingChains, resolver,
					missingEntitiesStrategy);
		} catch (EntityNotFoundException e) {
			switch (missingEntitiesStrategy) {
			case BUILD_PARTIAL:
				// Can't happen in this case because checkRings won't throw this
				// exception with BUILD_PARTIAL
				break;
			case BUILD_EMPTY:
				return factory.createMultiPolygon(null);
			default:
			case THROW_EXCEPTION:
				throw (e);
			}
		}

		// This could be used to close non-closed chains
		// RelationUtil.closeUnclosedRingWithStraightLine(rings);

		MultiPolygon mp = buildMultipolygon(rings, resolver);

		Coordinate[] cs;
		if (!includePuntal) {
			cs = new Coordinate[0];
		} else {
			List<Coordinate> coords = GeometryUtil.buildNodes(nodeBuilder,
					nodes);
			cs = coords.toArray(new Coordinate[0]);
		}

		LineString[] ls;
		if (!includeLineal) {
			ls = new LineString[0];
		} else {
			ls = buildWays(nonRings, resolver);
		}

		return GeometryUtil.createGeometry(cs, ls, mp, factory);
	}

	private LineString[] buildWays(List<ChainOfNodes> nonRings,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		List<LineString> lines = new ArrayList<>();

		for (ChainOfNodes chain : nonRings) {
			List<Coordinate> coords = new ArrayList<>();
			TLongList nodes = chain.getNodes();
			TLongIterator iterator = nodes.iterator();
			while (iterator.hasNext()) {
				long id = iterator.next();
				OsmNode node = resolver.getNode(id);
				coords.add(nodeBuilder.buildCoordinate(node));
			}
			factory.createLinearRing(coords.toArray(new Coordinate[0]));
		}
		return lines.toArray(new LineString[0]);
	}

	private MultiPolygon buildMultipolygon(Collection<ChainOfNodes> rings,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		Set<Coordinate> coordinates = new HashSet<>();
		Set<LineString> lineStrings = new HashSet<>();
		Set<LinearRing> linearRings = new HashSet<>();
		toLinearRings(rings, resolver, coordinates, lineStrings, linearRings);

		Set<LinearRing> validRings = new HashSet<>();
		for (LinearRing r : linearRings) {
			Set<LinearRing> repaired = SelfIntersectionUtil.repair(r);
			for (LinearRing ring : repaired) {
				validRings.add(ring);
			}
		}

		return PolygonHelper.multipolygonFromRings(validRings, false);
	}

	private void toLinearRings(Collection<ChainOfNodes> rings,
			OsmEntityProvider resolver, Collection<Coordinate> coordinates,
			Collection<LineString> lineStrings,
			Collection<LinearRing> linearRings) throws EntityNotFoundException
	{
		for (ChainOfNodes ring : rings) {
			if (!ring.isValidRing()) {
				logger.warn("isValidRing() failed for ChainOfSegments, but this point should never be reached");
				continue;
			}

			TLongList nodeIds = ring.getNodes();
			Way way = new Way(-1, nodeIds);
			WayBuilderResult result = wayBuilder.buildResult(way, resolver);

			if (includePuntal) {
				coordinates.addAll(result.getCoordinates());
			}
			if (includeLineal) {
				lineStrings.addAll(result.getLineStrings());
			}
			if (result.getLinearRing() != null) {
				linearRings.add(result.getLinearRing());
			}
		}
	}

}

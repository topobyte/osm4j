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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class RelationBuilder
{

	private GeometryFactory factory;

	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;

	private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
	private boolean log = false;
	private LogLevel logLevel = LogLevel.WARN;

	public RelationBuilder(GeometryFactory factory)
	{
		this.factory = factory;
		nodeBuilder = new NodeBuilder(factory);
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

	public Geometry buildPointsAndLines(OsmRelation relation,
			OsmEntityProvider provider) throws EntityNotFoundException
	{
		EntityNotFoundStrategy enfs = Util.strategy(missingEntitiesStrategy,
				log, logLevel);

		EntityFinder finder = EntityFinders.create(provider, enfs);
		Set<OsmNode> nodes = new HashSet<>();
		Set<OsmWay> ways = new HashSet<>();
		try {
			finder.findMemberNodesAndWays(relation, nodes, ways);
		} catch (EntityNotFoundException e) {
			switch (missingEntitiesStrategy) {
			default:
			case THROW_EXCEPTION:
				throw (e);
			case BUILD_EMPTY:
				return newEmptyPoint();
			case BUILD_PARTIAL:
				// Can't happen, because we're using the IGNORE strategy in this
				// case
				break;
			}
		}

		return buildPointsAndLines(nodes, ways, provider);
	}

	public Geometry buildPointsAndLines(Collection<OsmRelation> relations,
			OsmEntityProvider provider) throws EntityNotFoundException
	{
		EntityNotFoundStrategy enfs = Util.strategy(missingEntitiesStrategy,
				log, logLevel);

		EntityFinder finder = EntityFinders.create(provider, enfs);
		Set<OsmNode> nodes = new HashSet<>();
		Set<OsmWay> ways = new HashSet<>();
		try {
			finder.findMemberNodesAndWays(relations, nodes, ways);
		} catch (EntityNotFoundException e) {
			switch (missingEntitiesStrategy) {
			default:
			case THROW_EXCEPTION:
				throw (e);
			case BUILD_EMPTY:
				return newEmptyPoint();
			case BUILD_PARTIAL:
				// Can't happen, because we're using the IGNORE strategy in this
				// case
				break;
			}
		}

		return buildPointsAndLines(nodes, ways, provider);
	}

	private Geometry buildPointsAndLines(Set<OsmNode> nodes, Set<OsmWay> ways,
			OsmEntityProvider provider) throws EntityNotFoundException
	{
		wayBuilder.setMissingEntitesStrategy(missingEntitiesStrategy);

		List<Coordinate> coords = GeometryUtil.buildNodes(nodeBuilder, nodes);

		List<Geometry> lines = new ArrayList<>();
		for (OsmWay way : ways) {
			Geometry line = wayBuilder.build(way, provider);
			if (!line.isEmpty()) {
				lines.add(line);
			}
		}

		Coordinate[] cs = coords.toArray(new Coordinate[0]);
		LineString[] ls = lines.toArray(new LineString[0]);
		return GeometryUtil.createGeometry(cs, ls, factory);
	}

	private Point newEmptyPoint()
	{
		return new Point(null, factory);
	}

}

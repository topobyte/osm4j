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
import java.util.Set;

import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class LineworkBuilder extends AbstractGeometryBuilder
{

	private NodeBuilder nodeBuilder;
	private WayBuilder wayBuilder;

	private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
	private MissingWayNodeStrategy missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE;
	private boolean log = false;
	private LogLevel logLevel = LogLevel.WARN;

	public LineworkBuilder()
	{
		this(new GeometryFactory());
	}

	public LineworkBuilder(GeometryFactory factory)
	{
		super(factory);
		nodeBuilder = new NodeBuilder(factory);
		wayBuilder = new WayBuilder(factory);
		wayBuilder.setMissingEntitiesStrategy(missingEntitiesStrategy);
		wayBuilder.setMissingWayNodeStrategy(missingWayNodeStrategy);
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

	public void setMissingEntitiesStrategy(
			MissingEntitiesStrategy missingEntitiesStrategy)
	{
		this.missingEntitiesStrategy = missingEntitiesStrategy;
		wayBuilder.setMissingEntitiesStrategy(missingEntitiesStrategy);
	}

	public MissingWayNodeStrategy getMissingWayNodeStrategy()
	{
		return missingWayNodeStrategy;
	}

	public void setMissingWayNodeStrategy(
			MissingWayNodeStrategy missingWayNodeStrategy)
	{
		this.missingWayNodeStrategy = missingWayNodeStrategy;
		wayBuilder.setMissingWayNodeStrategy(missingWayNodeStrategy);
	}

	public LineworkBuilderResult build(OsmRelation relation,
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
				return new LineworkBuilderResult();
			case BUILD_PARTIAL:
				// Can't happen, because we're using the IGNORE strategy in this
				// case
				break;
			}
		}

		return build(nodes, ways, provider);
	}

	public LineworkBuilderResult build(Collection<OsmRelation> relations,
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
				return new LineworkBuilderResult();
			case BUILD_PARTIAL:
				// Can't happen, because we're using the IGNORE strategy in this
				// case
				break;
			}
		}

		return build(nodes, ways, provider);
	}

	public LineworkBuilderResult build(Collection<OsmNode> nodes,
			Collection<OsmWay> ways, OsmEntityProvider provider)
			throws EntityNotFoundException
	{
		LineworkBuilderResult result = new LineworkBuilderResult();

		GeometryUtil.buildNodes(nodeBuilder, nodes, result.getCoordinates());

		for (OsmWay way : ways) {
			WayBuilderResult wbr = wayBuilder.build(way, provider);
			result.getCoordinates().addAll(wbr.getCoordinates());
			result.getLineStrings().addAll(wbr.getLineStrings());
			if (wbr.getLinearRing() != null) {
				result.getLineStrings().add(wbr.getLinearRing());
			}
		}

		return result;
	}

}

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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayBuilder
{

	final static Logger logger = LoggerFactory.getLogger(WayBuilder.class);

	private GeometryFactory factory;
	private NodeBuilder nodeBuilder;

	private MissingEntitiesStrategy missingEntitiesStrategy = MissingEntitiesStrategy.THROW_EXCEPTION;
	private MissingWayNodeStrategy missingWayNodeStrategy = MissingWayNodeStrategy.OMIT_VERTEX_FROM_POLYLINE;
	private boolean includePuntal = true;
	private boolean log = false;
	private LogLevel logLevel = LogLevel.WARN;

	public WayBuilder()
	{
		this(new GeometryFactory());
	}

	public WayBuilder(GeometryFactory factory)
	{
		this.factory = factory;
		nodeBuilder = new NodeBuilder(factory);
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

	public MissingWayNodeStrategy getMissingWayNodeStrategy()
	{
		return missingWayNodeStrategy;
	}

	public void setMissingWayNodeStrategy(
			MissingWayNodeStrategy missingWayNodeStrategy)
	{
		this.missingWayNodeStrategy = missingWayNodeStrategy;
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

	/**
	 * Build a LinesString from the given way.
	 * 
	 * @param way
	 *            the way to use for building.
	 * @return the constructed LineString.
	 * @throws EntityNotFoundException
	 *             if a node cannot be resolved.
	 */
	public Geometry build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		switch (missingEntitiesStrategy) {
		default:
		case THROW_EXCEPTION:
			return buildThrowExceptionIfNodeMissing(way, resolver);
		case BUILD_EMPTY:
			return buildReturnEmptyIfNodeMissing(way, resolver);
		case BUILD_PARTIAL:
			switch (missingWayNodeStrategy) {
			default:
			case OMIT_VERTEX_FROM_POLYLINE:
				return buildOmitVertexIfNodeMissing(way, resolver);
			case SPLIT_POLYLINE:
				return buildSplitIfNodeMissing(way, resolver);
			}
		}
	}

	public Geometry buildThrowExceptionIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		int numNodes = way.getNumberOfNodes();
		if (numNodes == 0) {
			return newEmptyLineString();
		}
		if (numNodes == 1) {
			if (!includePuntal) {
				return newEmptyLineString();
			} else {
				OsmNode node = resolver.getNode(way.getNodeId(0));
				return nodeBuilder.build(node);
			}
		}

		CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(
				numNodes, 2);

		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			OsmNode node = resolver.getNode(way.getNodeId(i));
			cs.setOrdinate(i, 0, node.getLongitude());
			cs.setOrdinate(i, 1, node.getLatitude());
		}
		return factory.createLineString(cs);
	}

	public Geometry buildReturnEmptyIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		int numNodes = way.getNumberOfNodes();
		if (numNodes == 0) {
			return newEmptyLineString();
		}
		if (numNodes == 1) {
			if (!includePuntal) {
				return newEmptyLineString();
			} else {
				try {
					OsmNode node = resolver.getNode(way.getNodeId(0));
					return nodeBuilder.build(node);
				} catch (EntityNotFoundException e) {
					return newEmptyLineString();
				}
			}
		}

		CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(
				numNodes, 2);

		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			OsmNode node;
			try {
				node = resolver.getNode(way.getNodeId(i));
			} catch (EntityNotFoundException e) {
				return newEmptyLineString();
			}
			cs.setOrdinate(i, 0, node.getLongitude());
			cs.setOrdinate(i, 1, node.getLatitude());
		}
		return factory.createLineString(cs);
	}

	public Geometry buildOmitVertexIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		List<Coordinate> coords = new ArrayList<>();
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			OsmNode node;
			try {
				node = resolver.getNode(way.getNodeId(i));
			} catch (EntityNotFoundException e) {
				if (log) {
					logMissingNode(way.getNodeId(i));
				}
				continue;
			}
			coords.add(new Coordinate(node.getLongitude(), node.getLatitude()));
		}

		if (coords.size() == 0) {
			return newEmptyLineString();
		}
		if (coords.size() == 1) {
			if (!includePuntal) {
				return newEmptyLineString();
			} else {
				return factory.createPoint(coords.get(0));
			}
		}

		CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(
				coords.toArray(new Coordinate[0]));
		return factory.createLineString(cs);
	}

	public Geometry buildSplitIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		CoordinateSequencesBuilder builder = new CoordinateSequencesBuilder();
		builder.beginNewSequence();

		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			OsmNode node;
			try {
				node = resolver.getNode(way.getNodeId(i));
			} catch (EntityNotFoundException e) {
				if (log) {
					logMissingNode(way.getNodeId(i));
				}
				builder.beginNewSequence();
				continue;
			}
			builder.add(new Coordinate(node.getLongitude(), node.getLatitude()));
		}
		builder.finishSequence();

		return builder.createGeometry(factory, includePuntal);
	}

	private void logMissingNode(long nodeId)
	{
		String message = String.format("Node not found: %d", nodeId);
		log(message);
	}

	private void log(String message)
	{
		switch (logLevel) {
		default:
		case INFO:
			logger.info(message);
			break;
		case DEBUG:
			logger.debug(message);
			break;
		case WARN:
			logger.warn(message);
			break;
		}
	}

	private LineString newEmptyLineString()
	{
		return new LineString(null, factory);
	}

}

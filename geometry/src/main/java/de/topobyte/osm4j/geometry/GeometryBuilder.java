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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class GeometryBuilder
{

	private NodeBuilder nodeBuilder;
	private WayBuilder polylineBuilder;
	private RegionBuilder regionBuilder;

	public GeometryBuilder()
	{
		this(new GeometryFactory());
	}

	public GeometryBuilder(GeometryFactory factory)
	{
		nodeBuilder = new NodeBuilder(factory);
		polylineBuilder = new WayBuilder(factory);
		regionBuilder = new RegionBuilder(factory);
	}

	/**
	 * Build a Coordinate from the given node.
	 * 
	 * @param node
	 *            the node to use for building.
	 * @return the constructed Coordinate.
	 */
	public Coordinate buildCoordinate(OsmNode node)
	{
		return nodeBuilder.buildCoordinate(node);
	}

	/**
	 * Build a Point from the given node.
	 * 
	 * @param node
	 *            the node to use for building.
	 * @return the constructed Point.
	 */
	public Point build(OsmNode node)
	{
		return nodeBuilder.build(node);
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
		return polylineBuilder.buildThrowExceptionIfNodeMissing(way, resolver);
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
		regionBuilder
				.setMissingEntitesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
		return regionBuilder.build(relation, resolver);
	}

}

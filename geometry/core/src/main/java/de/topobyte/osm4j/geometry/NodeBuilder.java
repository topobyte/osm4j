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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import de.topobyte.osm4j.core.model.iface.OsmNode;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class NodeBuilder
{

	private GeometryFactory factory;

	public NodeBuilder()
	{
		this(new GeometryFactory());
	}

	public NodeBuilder(GeometryFactory factory)
	{
		this.factory = factory;
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
		double lon = node.getLongitude();
		double lat = node.getLatitude();
		return new Coordinate(lon, lat);
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
		return factory.createPoint(buildCoordinate(node));
	}

}

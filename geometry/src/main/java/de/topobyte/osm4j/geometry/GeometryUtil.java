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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class GeometryUtil
{

	public static Geometry createGeometry(Coordinate[] coordinates,
			LineString[] lineStrings, GeometryFactory factory)
	{
		int numPoints = coordinates.length;
		int numLines = lineStrings.length;

		if (numPoints == 0 && numLines == 0) {
			return new Point(null, factory);
		} else if (numPoints == 0) {
			return lines(lineStrings, factory);
		} else if (numLines == 0) {
			return points(coordinates, factory);
		} else {
			Geometry points = points(coordinates, factory);
			Geometry lines = lines(lineStrings, factory);
			return factory.createGeometryCollection(new Geometry[] { points,
					lines });
		}
	}

	public static Geometry points(Coordinate[] coordinates,
			GeometryFactory factory)
	{
		if (coordinates.length == 1) {
			return factory.createPoint(coordinates[0]);
		}
		return factory.createMultiPoint(coordinates);
	}

	public static Geometry lines(LineString[] lineStrings,
			GeometryFactory factory)
	{
		if (lineStrings.length == 1) {
			return lineStrings[0];
		}
		return factory.createMultiLineString(lineStrings);
	}

}

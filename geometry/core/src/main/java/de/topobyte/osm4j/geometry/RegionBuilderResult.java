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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;

import de.topobyte.jts.utils.GeometryGroup;

public class RegionBuilderResult
{

	private List<Coordinate> coordinates = new ArrayList<>();
	private List<LineString> lineStrings = new ArrayList<>();
	private MultiPolygon multiPolygon = null;

	public List<Coordinate> getCoordinates()
	{
		return coordinates;
	}

	public void setCoordinates(List<Coordinate> coordinates)
	{
		this.coordinates = coordinates;
	}

	public List<LineString> getLineStrings()
	{
		return lineStrings;
	}

	public void setLineStrings(List<LineString> lineStrings)
	{
		this.lineStrings = lineStrings;
	}

	public MultiPolygon getMultiPolygon()
	{
		return multiPolygon;
	}

	public void setMultiPolygon(MultiPolygon multiPolygon)
	{
		this.multiPolygon = multiPolygon;
	}

	public void clear()
	{
		coordinates.clear();
		lineStrings.clear();
		multiPolygon = null;
	}

	public Geometry toGeometry(GeometryFactory factory)
	{
		if (multiPolygon == null) {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					factory);
		} else {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					multiPolygon, factory);
		}
	}

	public GeometryGroup toGeometryGroup(GeometryFactory factory)
	{
		if (multiPolygon == null) {
			return GeometryUtil.createGeometryGroup(coordinates, lineStrings,
					factory);
		} else {
			return GeometryUtil.createGeometryGroup(coordinates, lineStrings,
					multiPolygon, factory);
		}
	}

}

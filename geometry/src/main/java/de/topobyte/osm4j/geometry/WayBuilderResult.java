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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

public class WayBuilderResult
{

	private List<Coordinate> coordinates = new ArrayList<>();
	private List<LineString> lineStrings = new ArrayList<>();
	private LinearRing linearRing = null;

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

	public LinearRing getLinearRing()
	{
		return linearRing;
	}

	public void setLinearRing(LinearRing linearRing)
	{
		this.linearRing = linearRing;
	}

	public void clear()
	{
		coordinates.clear();
		lineStrings.clear();
		linearRing = null;
	}

	public Geometry toGeometry(GeometryFactory factory)
	{
		Coordinate[] coordinates = this.coordinates.toArray(new Coordinate[0]);
		LineString[] lineStrings = this.lineStrings.toArray(new LineString[0]);
		if (linearRing == null) {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					factory);
		} else {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					linearRing, factory);
		}
	}

}

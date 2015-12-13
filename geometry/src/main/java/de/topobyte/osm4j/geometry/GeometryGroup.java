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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GeometryGroup extends GeometryCollection
{

	private static final long serialVersionUID = 20100485742596114L;

	public GeometryGroup(GeometryFactory factory, Geometry... geometries)
	{
		super(geometries, factory);
	}

	@Override
	public boolean intersects(Geometry g)
	{
		for (Geometry geometry : geometries) {
			if (g.intersects(geometry)) {
				return true;
			}
		}
		return false;
	}

}

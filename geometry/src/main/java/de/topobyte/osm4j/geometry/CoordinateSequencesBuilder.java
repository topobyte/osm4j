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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

class CoordinateSequencesBuilder
{

	private List<List<Coordinate>> results = new ArrayList<>();
	private List<Coordinate> current = null;

	public void beginNewSequence()
	{
		finish(true);
	}

	public void finishSequence()
	{
		finish(false);
	}

	private void finish(boolean initNewList)
	{
		if (current != null && !current.isEmpty()) {
			results.add(current);
		}
		if (initNewList) {
			current = new ArrayList<>();
		} else {
			current = null;
		}
	}

	public void add(Coordinate c)
	{
		current.add(c);
	}

	public Geometry createGeometry(GeometryFactory factory,
			boolean includePuntal)
	{
		int numPoints = 0;
		int numLines = 0;
		for (int i = 0; i < results.size(); i++) {
			List<Coordinate> coords = results.get(i);
			if (coords.size() == 1) {
				numPoints++;
			} else {
				numLines++;
			}
		}

		Coordinate[] points = new Coordinate[numPoints];
		LineString[] lineStrings = new LineString[numLines];

		int indexPoints = 0;
		int indexLines = 0;

		for (List<Coordinate> coords : results) {
			if (coords.size() == 1) {
				points[indexPoints] = coords.get(0);
			} else {
				CoordinateSequence cs = factory.getCoordinateSequenceFactory()
						.create(coords.toArray(new Coordinate[0]));
				lineStrings[indexLines] = factory.createLineString(cs);
			}
		}

		return GeometryUtil.createGeometry(points, lineStrings, factory);
	}

}

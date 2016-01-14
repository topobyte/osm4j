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
import com.vividsolutions.jts.geom.GeometryFactory;

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

	public WayBuilderResult createWayBuilderResult(GeometryFactory factory,
			boolean includePuntal, boolean closed, boolean firstMissing)
	{
		WayBuilderResult result = new WayBuilderResult();

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

		// If the input way is closed, the first coordinate is not missing and
		// there have been no gaps (only one line constructed), then we try to
		// create a linear ring instead of a line string.
		if (closed && !firstMissing && numPoints == 0 && numLines == 1) {
			List<Coordinate> coords = results.get(0);
			if (coords.size() > 3) {
				result.setLinearRing(factory.createLinearRing(coords
						.toArray(new Coordinate[0])));
				return result;
			}
		}

		// Indices to the first and last list of coordinates.
		int first = 0;
		int last = results.size() - 1;

		// If the input way is closed and the first coordinate is not missing,
		// combine the first and the last chain of coordinates to a single
		// segment without the common coordinate repeated.
		if (closed && !firstMissing) {
			List<Coordinate> coords = new ArrayList<>();
			List<Coordinate> c1 = results.get(first);
			List<Coordinate> c2 = results.get(last);
			coords.addAll(c2);
			coords.addAll(c1.subList(1, c1.size()));

			result.getLineStrings()
					.add(factory.createLineString(coords
							.toArray(new Coordinate[0])));

			first++;
			last--;

			if (c1.size() == 1) {
				numPoints--;
			} else {
				numLines--;
			}
			if (c2.size() == 1) {
				numPoints--;
			} else {
				numLines--;
			}
		}

		for (int i = first; i <= last; i++) {
			List<Coordinate> coords = results.get(i);
			if (coords.size() == 1) {
				result.getCoordinates().add(coords.get(0));
			} else {
				CoordinateSequence cs = factory.getCoordinateSequenceFactory()
						.create(coords.toArray(new Coordinate[0]));
				result.getLineStrings().add(factory.createLineString(cs));
			}
		}

		return result;
	}

}

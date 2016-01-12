// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.utils;

import java.io.IOException;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.utils.bbox.BBoxCalculator;

public class OsmUtils
{

	public static BBox computeBBox(OsmIteratorInputFactory iteratorFactory)
			throws IOException
	{
		OsmIteratorInput iterator = iteratorFactory
				.createIterator(false, false);
		BBoxCalculator calculator = new BBoxCalculator(iterator.getIterator());
		BBox bbox = calculator.execute();
		iterator.close();
		return bbox;
	}

}

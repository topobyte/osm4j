// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.utils.areafilter;

import de.topobyte.adt.geo.BBox;
import de.topobyte.jts.utils.predicate.PredicateEvaluatorRectangle;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;

public class BboxFilter extends AbstractAreaFilter
{

	public BboxFilter(OsmOutputStream output, OsmIterator input, BBox bbox,
			boolean onlyNodes)
	{
		super(output, input, onlyNodes);

		test = new PredicateEvaluatorRectangle(bbox.getLon1(), bbox.getLat2(),
				bbox.getLon2(), bbox.getLat1());
	}

}

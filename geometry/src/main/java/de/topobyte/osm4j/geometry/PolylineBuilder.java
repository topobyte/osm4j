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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class PolylineBuilder
{

	final static Logger logger = LoggerFactory.getLogger(PolylineBuilder.class);

	/**
	 * Build a LinesString from the given way.
	 * 
	 * @param way
	 *            the way to use for building.
	 * @return the constructed LineString.
	 * @throws EntityNotFoundException
	 *             if a node cannot be resolved.
	 */
	public static LineString build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		GeometryFactory factory = new GeometryFactory();

		CoordinateSequence cs = factory.getCoordinateSequenceFactory().create(
				way.getNumberOfNodes(), 2);

		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			OsmNode node = resolver.getNode(way.getNodeId(i));
			cs.setOrdinate(i, 0, node.getLongitude());
			cs.setOrdinate(i, 1, node.getLatitude());
		}
		LineString lineString = factory.createLineString(cs);

		return lineString;
	}

}

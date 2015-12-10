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

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayBuilder extends WayGeometryBuilder
{

	public WayBuilder()
	{
		super();
	}

	public WayBuilder(GeometryFactory factory)
	{
		super(factory);
	}

	/**
	 * Build a LinesString from the given way.
	 * 
	 * @param way
	 *            the way to use for building.
	 * @return the constructed LineString.
	 * @throws EntityNotFoundException
	 *             if a node cannot be resolved.
	 */
	public Geometry build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		WayBuilderResult result = buildResult(way, resolver);
		return geometry(result);
	}

	public Geometry buildThrowExceptionIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		return geometry(buildResultThrowExceptionIfNodeMissing(way, resolver));
	}

	public Geometry buildReturnEmptyIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		return geometry(buildResultReturnEmptyIfNodeMissing(way, resolver));
	}

	public Geometry buildOmitVertexIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		return geometry(buildResultOmitVertexIfNodeMissing(way, resolver));
	}

	public Geometry buildSplitIfNodeMissing(OsmWay way,
			OsmEntityProvider resolver)
	{
		return geometry(buildResultSplitIfNodeMissing(way, resolver));
	}

	private Geometry geometry(WayBuilderResult result)
	{
		Coordinate[] coordinates = result.getCoordinates().toArray(
				new Coordinate[0]);
		LineString[] lineStrings = result.getLineStrings().toArray(
				new LineString[0]);
		if (result.getLinearRing() == null) {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					factory);
		} else {
			return GeometryUtil.createGeometry(coordinates, lineStrings,
					result.getLinearRing(), factory);
		}
	}

}

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

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class RegionBuilder extends RegionGeometryBuilder
{

	public RegionBuilder()
	{
		this(new GeometryFactory());
	}

	public RegionBuilder(GeometryFactory factory)
	{
		super(factory);
	}

	/**
	 * Build a MultiPolygon from a Relation.
	 * 
	 * @param relation
	 *            a relation to construct the region for.
	 * @return the constructed MultiPolygon.
	 * @throws EntityNotFoundException
	 *             when a required entity cannot be obtained.
	 */
	public Geometry build(OsmRelation relation, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		RegionBuilderResult result = buildResult(relation, resolver);
		return GeometryUtil.createGeometry(result.getCoordinates(),
				result.getLineStrings(), result.getMultiPolygon(), factory);
	}

	public Geometry build(OsmWay way, OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		RegionBuilderResult result = buildResult(way, resolver);
		return GeometryUtil.createGeometry(result.getCoordinates(),
				result.getLineStrings(), result.getMultiPolygon(), factory);
	}

	/**
	 * Build a MultiPolygon from a Set of Ways.
	 * 
	 * @param ways
	 *            the ways to use for geometry construction.
	 * @return the constructed MultiPolygon.
	 * @throws EntityNotFoundException
	 *             when a required entity cannot be obtained.
	 */
	public Geometry build(MultiSet<OsmWay> ways, OsmEntityProvider resolver,
			Set<OsmNode> nodes) throws EntityNotFoundException
	{
		RegionBuilderResult result = buildResult(ways, resolver, nodes);
		return GeometryUtil.createGeometry(result.getCoordinates(),
				result.getLineStrings(), result.getMultiPolygon(), factory);
	}

}

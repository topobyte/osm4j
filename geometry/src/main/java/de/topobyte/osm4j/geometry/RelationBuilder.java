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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.GeometryBuilder;

public class RelationBuilder
{

	public static Geometry buildPointsAndLines(OsmRelation relation,
			OsmEntityProvider provider)
	{
		EntityFinder finder = EntityFinders.create(provider,
				EntityNotFoundStrategy.IGNORE);
		Set<OsmNode> nodes = new HashSet<>();
		Set<OsmWay> ways = new HashSet<>();
		try {
			finder.findMemberNodesAndWays(relation, nodes, ways);
		} catch (EntityNotFoundException e) {
			// Can't happen because we're using the IGNORE strategy
		}

		return buildPointsAndLines(nodes, ways, provider);
	}

	public static GeometryCollection buildPointsAndLines(
			Collection<OsmRelation> relations, OsmEntityProvider provider)
	{
		EntityFinder finder = EntityFinders.create(provider,
				EntityNotFoundStrategy.IGNORE);
		Set<OsmNode> nodes = new HashSet<>();
		Set<OsmWay> ways = new HashSet<>();
		try {
			finder.findMemberNodesAndWays(relations, nodes, ways);
		} catch (EntityNotFoundException e) {
			// Can't happen because we're using the IGNORE strategy
		}

		return buildPointsAndLines(nodes, ways, provider);
	}

	private static GeometryCollection buildPointsAndLines(Set<OsmNode> nodes,
			Set<OsmWay> ways, OsmEntityProvider provider)
	{
		List<Coordinate> coords = new ArrayList<>();
		for (OsmNode node : nodes) {
			coords.add(GeometryBuilder.buildCoordinate(node));
		}

		List<LineString> lines = new ArrayList<>();
		for (OsmWay way : ways) {
			try {
				lines.add(GeometryBuilder.build(way, provider));
			} catch (EntityNotFoundException e) {
				// TODO: we need an IGNORE strategy for the GeometryBuilder
			}
		}

		GeometryFactory f = new GeometryFactory();

		MultiPoint multiPoint = f.createMultiPoint(coords
				.toArray(new Coordinate[0]));
		MultiLineString multiLine = f.createMultiLineString(lines
				.toArray(new LineString[0]));

		return f.createGeometryCollection(new Geometry[] { multiPoint,
				multiLine });
	}

}

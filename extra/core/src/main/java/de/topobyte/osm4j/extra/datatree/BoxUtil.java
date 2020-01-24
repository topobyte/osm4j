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

package de.topobyte.osm4j.extra.datatree;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

public class BoxUtil
{

	public static Envelope WORLD_BOUNDS = new Envelope(-180, 180, -86, 86);

	public static GeometryCollection createBoxesGeometry(DataTree tree,
			Envelope bound)
	{
		return createBoxesGeometry(tree.getLeafs(), bound);
	}

	public static GeometryCollection createBoxesGeometry(List<Node> leafs,
			Envelope bound)
	{
		GeometryFactory factory = new GeometryFactory();

		Geometry[] boxes = new Geometry[leafs.size()];
		for (int i = 0; i < leafs.size(); i++) {
			Node leaf = leafs.get(i);
			Envelope envelope = leaf.getEnvelope().intersection(bound);
			Geometry g = factory.toGeometry(envelope);
			boxes[i] = g;
		}

		return factory.createGeometryCollection(boxes);
	}

}

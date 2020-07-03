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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.io.WKTWriter;

public class TestDataTree
{

	public static void main(String[] args) throws IOException
	{
		test(new Envelope(-180, 180, -90, 90), 14);
		test(new Envelope(10, 20, -8, 24), 5);
	}

	private static void test(Envelope box, int splitDepth) throws IOException
	{
		DataTree tree = new DataTree(box);
		tree.getRoot().split(splitDepth);
		tree.print();

		File fileOutput = File.createTempFile("tree", ".wkt");

		GeometryCollection geometry = BoxUtil.createBoxesGeometry(tree,
				BoxUtil.WORLD_BOUNDS);

		System.out.println("Writing output to: " + fileOutput);

		FileWriter writer = new FileWriter(fileOutput);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

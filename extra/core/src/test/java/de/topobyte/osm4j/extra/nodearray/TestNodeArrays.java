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

package de.topobyte.osm4j.extra.nodearray;

import static org.junit.Assert.assertEquals;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.extra.nodearray.util.Intervals;

public class TestNodeArrays
{

	@Test
	public void testDouble() throws IOException
	{
		test(Factories.DOUBLE);
	}

	@Test
	public void testFloat() throws IOException
	{
		test(Factories.FLOAT);
	}

	@Test
	public void testInteger() throws IOException
	{
		test(Factories.INTEGER);
	}

	@Test
	public void testShort() throws IOException
	{
		test(Factories.SHORT);
	}

	private void test(Factory factory) throws IOException
	{
		File file = File.createTempFile("nodearray", ".dat");
		test(file, factory);
		file.delete();
	}

	private void test(File file, Factory factory) throws IOException
	{
		OutputStream fos = new FileOutputStream(file);
		DataOutputStream out = new DataOutputStream(fos);
		NodeArrayWriter writer = factory.createWriter(out);

		int n = 1000;
		int step = 10;

		List<OsmNode> nodes = new ArrayList<>();
		long lastId = 0;
		Random random = new Random();
		for (int i = 0; i < n; i++) {
			long id = lastId + 1 + random.nextInt(step);
			double lon = Intervals.random(Intervals.LONGITUDE);
			double lat = Intervals.random(Intervals.LATITUDE);
			nodes.add(new Node(id, lon, lat));
			lastId = id;
		}

		for (OsmNode node : nodes) {
			writer.write(node);
		}
		writer.finish();

		NodeArray array = factory.createNodeArray(file);
		for (OsmNode node : nodes) {
			OsmNode copy = array.get(node.getId());

			assertEquals(factory.getExpectedValue(node.getLatitude()),
					copy.getLatitude(), factory.getErrorDeltaLat());
			assertEquals(factory.getExpectedValue(node.getLongitude()),
					copy.getLongitude(), factory.getErrorDeltaLon());
		}
	}

}

// Copyright 2018 Sebastian Kuerten
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

package de.topobyte.osm4j.pbf;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.model.TestNode;

public class TestMissingCoordinates
{

	// The file 'data-with-missing-coordinates.pbf' has been created from
	// 'data-with-missing-coordinates.osm' using 'osmium cat'. Although in
	// the original file node 2 had only its latitude removed and node 4
	// only its longitude, the resulting pbf file contains neither longitude
	// nor latitude for both of them. Hence the check assumes both lat and
	// lon to be null, encoded as Double.NAN.
	private String resource = "data-with-missing-coordinates.pbf";

	@Test
	public void testReader() throws IOException, OsmInputException
	{
		PbfReader reader = Util.reader(resource, true);
		TestDataSet data = DataSetHelper.read(reader);
		test(data);
	}

	@Test
	public void testIterator() throws IOException
	{
		OsmIterator iterator = Util.iterator(resource, true);
		TestDataSet data = DataSetHelper.read(iterator);
		test(data);
	}

	protected void test(TestDataSet data)
	{
		List<TestNode> nodes = data.getNodes();
		TestNode nodeWithoutLat = nodes.get(2);
		TestNode nodeWithoutLon = nodes.get(4);
		TestNode nodeWithoutLonLat = nodes.get(7);

		assertCoordinatesNull(nodeWithoutLat);
		assertCoordinatesNull(nodeWithoutLon);
		assertCoordinatesNull(nodeWithoutLonLat);

		assertCoordinatesNotNull(nodes.get(0));
		assertCoordinatesNotNull(nodes.get(1));
		assertCoordinatesNotNull(nodes.get(3));
		assertCoordinatesNotNull(nodes.get(5));
		assertCoordinatesNotNull(nodes.get(6));
		assertCoordinatesNotNull(nodes.get(8));
		assertCoordinatesNotNull(nodes.get(9));
	}

	private void assertCoordinatesNotNull(TestNode node)
	{
		double lat = node.getLatitude();
		double lon = node.getLongitude();
		Assert.assertFalse("lat is not null", isNull(lat));
		Assert.assertFalse("lon is not null", isNull(lon));
	}

	private void assertCoordinatesNull(TestNode node)
	{
		double lat = node.getLatitude();
		double lon = node.getLongitude();
		Assert.assertTrue("lat is null", isNull(lat));
		Assert.assertTrue("lon is null", isNull(lon));
	}

	private boolean isNull(double number)
	{
		return Double.isNaN(number);
	}

}

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

package de.topobyte.osm4j.testing;

import org.junit.Assert;
import org.junit.Test;

public class TestDataSetCopy
{

	@Test
	public void test()
	{
		EntityGenerator entityGenerator = new EntityGenerator(10, true);
		DataSetGenerator dataSetGenerator = new DataSetGenerator(
				entityGenerator);

		int n = 10;

		TestDataSet data = dataSetGenerator.generate(n, n, n);
		TestDataSet copy = new TestDataSet(data);

		Assert.assertEquals(data.getNodes().size(), n);
		Assert.assertEquals(data.getWays().size(), n);
		Assert.assertEquals(data.getRelations().size(), n);

		Assert.assertTrue(DataSetHelper.equals(data, copy));
		Assert.assertTrue(DataSetHelper.equals(copy, data));
	}

}

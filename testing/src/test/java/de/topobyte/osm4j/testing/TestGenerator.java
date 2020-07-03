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

public class TestGenerator
{

	@Test
	public void test()
	{
		EntityGenerator entityGenerator = new EntityGenerator(10, true);
		DataSetGenerator dataSetGenerator = new DataSetGenerator(
				entityGenerator);

		int numNodes = 30;
		int numWays = 20;
		int numRelations = 10;

		TestDataSet data = dataSetGenerator.generate(numNodes, numWays,
				numRelations);

		Assert.assertEquals(data.getNodes().size(), numNodes);
		Assert.assertEquals(data.getWays().size(), numWays);
		Assert.assertEquals(data.getRelations().size(), numRelations);
	}

}

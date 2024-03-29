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

package de.topobyte.osm4j.utils.sort;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.TestDataSetIterator;

public class TestMemorySortIterator extends BaseTest
{

	@Test
	public void test()
	{
		test(100, 100, 100, 0);
		test(0, 100, 100, 0);
		test(100, 0, 100, 0);
		test(100, 100, 0, 0);
		test(100, 0, 0, 0);
		test(0, 100, 0, 0);
		test(0, 0, 100, 0);

		double p = 0.1;
		test(100, 100, 100, p);
		test(0, 100, 100, p);
		test(100, 0, 100, p);
		test(100, 100, 0, p);
		test(100, 0, 0, p);
		test(0, 100, 0, p);
		test(0, 0, 100, p);
	}

	public void test(int numNodes, int numWays, int numRelations,
			double fractionDuplicates)
	{
		do {
			setup(numNodes, numWays, numRelations, fractionDuplicates);
		} while (DataSetHelper.equals(data, shuffled));

		MemorySortIterator sorter = new MemorySortIterator(
				new TestDataSetIterator(shuffled));
		TestDataSet sorted = DataSetHelper.read(sorter);

		Assert.assertTrue(DataSetHelper.equals(data, sorted));
	}

}

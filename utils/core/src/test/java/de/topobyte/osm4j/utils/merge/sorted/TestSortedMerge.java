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

package de.topobyte.osm4j.utils.merge.sorted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.TestDataSetIterator;
import de.topobyte.osm4j.testing.TestDataSetOutputStream;

public class TestSortedMerge extends BaseTest
{

	@Test
	public void test() throws IOException
	{
		test(100, 100, 100, 10, 0.8);
		test(100, 100, 100, 10, 0.5);
		test(100, 100, 100, 10, 0.1);
		test(100, 100, 100, 1, 0.5);
		test(100, 100, 100, 2, 0.5);
		test(100, 0, 0, 5, 0.5);
		test(0, 100, 0, 5, 0.5);
		test(0, 0, 100, 5, 0.5);
	}

	public void test(int numNodes, int numWays, int numRelations, int numFiles,
			double p) throws IOException
	{
		setup(numNodes, numWays, numRelations, numFiles, p);

		TestDataSetOutputStream output = new TestDataSetOutputStream();

		List<OsmIterator> inputs = new ArrayList<>();
		for (TestDataSet dataSet : dataSets) {
			inputs.add(new TestDataSetIterator(dataSet));
		}

		SortedMerge merge = new SortedMerge(output, inputs);
		merge.run();
		TestDataSet merged = output.getData();

		Assert.assertEquals(data.getNodes().size(), merged.getNodes().size());
		Assert.assertEquals(data.getWays().size(), merged.getWays().size());
		Assert.assertEquals(data.getRelations().size(), merged.getRelations()
				.size());
		Assert.assertTrue(DataSetHelper.equals(data, merged));
	}

}

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

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.EntityGenerator;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.TestDataSetIterator;
import de.topobyte.osm4j.testing.TestDataSetOutputStream;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestWay;

public class TestSortedMerge
{

	private Random random = new Random();
	private EntityGenerator entityGenerator;
	private DataSetGenerator dataSetGenerator;
	private TestDataSet data;

	private List<TestDataSet> dataSets;

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

	public void setup(int numNodes, int numWays, int numRelations,
			int numFiles, double p) throws IOException
	{
		dataSets = new ArrayList<>();

		// Generate some data
		entityGenerator = new EntityGenerator(10, true);
		dataSetGenerator = new DataSetGenerator(entityGenerator);
		data = dataSetGenerator.generate(numNodes, numWays, numRelations);

		// Create some data sets
		for (int i = 0; i < numFiles; i++) {
			dataSets.add(new TestDataSet());
		}

		// Build separate data sets from the generated one, with data overlap
		for (TestNode n : data.getNodes()) {
			for (int i : pick(numFiles, p)) {
				dataSets.get(i).getNodes().add(n);
			}
		}
		for (TestWay w : data.getWays()) {
			for (int i : pick(numFiles, p)) {
				dataSets.get(i).getWays().add(w);
			}
		}
		for (TestRelation r : data.getRelations()) {
			for (int i : pick(numFiles, p)) {
				dataSets.get(i).getRelations().add(r);
			}
		}
	}

	private int[] pick(int n, double p)
	{
		TIntSet set = new TIntHashSet();
		// Pick at least one of the numbers
		set.add(random.nextInt(n));
		// And add some additional ones randomly
		for (int i = 0; i < n; i++) {
			if (random.nextDouble() > p) {
				set.add(i);
			}
		}
		return set.toArray();
	}

}

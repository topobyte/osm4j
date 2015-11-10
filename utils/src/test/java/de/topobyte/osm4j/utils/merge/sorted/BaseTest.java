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

import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.EntityGenerator;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestWay;

class BaseTest
{

	private Random random = new Random();
	private EntityGenerator entityGenerator;
	private DataSetGenerator dataSetGenerator;

	protected TestDataSet data;
	protected List<TestDataSet> dataSets;

	protected void setup(int numNodes, int numWays, int numRelations,
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

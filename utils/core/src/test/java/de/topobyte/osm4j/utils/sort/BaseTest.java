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

import java.io.IOException;
import java.util.Collections;
import java.util.Random;

import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.EntityGenerator;
import de.topobyte.osm4j.testing.TestDataSet;

public class BaseTest
{

	private Random random = new Random();
	private EntityGenerator entityGenerator;
	private DataSetGenerator dataSetGenerator;

	protected TestDataSet data;
	protected TestDataSet shuffled;

	protected void setup(int numNodes, int numWays, int numRelations,
			double fractionDuplicates) throws IOException
	{
		// Generate some data
		entityGenerator = new EntityGenerator(10, true);
		dataSetGenerator = new DataSetGenerator(entityGenerator);
		data = dataSetGenerator.generate(numNodes, numWays, numRelations);

		// Shuffle data
		shuffled = new TestDataSet(data);

		int nDupNodes = (int) Math.round(fractionDuplicates * numNodes);
		int nDupWays = (int) Math.round(fractionDuplicates * numWays);
		int nDupRelations = (int) Math.round(fractionDuplicates * numRelations);

		for (int i = 0; i < nDupNodes; i++) {
			int index = random.nextInt(numNodes);
			shuffled.getNodes().add(data.getNodes().get(index));
		}
		for (int i = 0; i < nDupWays; i++) {
			int index = random.nextInt(numWays);
			shuffled.getWays().add(data.getWays().get(index));
		}
		for (int i = 0; i < nDupRelations; i++) {
			int index = random.nextInt(numRelations);
			shuffled.getRelations().add(data.getRelations().get(index));
		}

		Collections.shuffle(shuffled.getNodes(), random);
		Collections.shuffle(shuffled.getWays(), random);
		Collections.shuffle(shuffled.getRelations(), random);
	}

}

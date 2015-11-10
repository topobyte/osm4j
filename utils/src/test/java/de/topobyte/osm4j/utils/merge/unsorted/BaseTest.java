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

package de.topobyte.osm4j.utils.merge.unsorted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.EntityGenerator;
import de.topobyte.osm4j.testing.TestDataSet;

public class BaseTest
{

	private EntityGenerator entityGenerator;
	private DataSetGenerator dataSetGenerator;

	protected TestDataSet data;
	protected List<TestDataSet> dataSets;

	public void setup(int numNodes, int numWays, int numRelations, int numFiles)
			throws IOException
	{
		dataSets = new ArrayList<>();

		// Generate some data
		entityGenerator = new EntityGenerator(10, true);
		dataSetGenerator = new DataSetGenerator(entityGenerator);

		// Create some data sets
		for (int i = 0; i < numFiles; i++) {
			dataSets.add(dataSetGenerator.generate(numNodes, numWays,
					numRelations));
		}

		data = new TestDataSet();

		// Build merged data set
		for (TestDataSet dataSet : dataSets) {
			data.getNodes().addAll(dataSet.getNodes());
			data.getWays().addAll(dataSet.getWays());
			data.getRelations().addAll(dataSet.getRelations());
		}
	}

}

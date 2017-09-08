// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.xml.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.EntityGenerator;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class TestRewriteRandom
{

	@Test
	public void test() throws IOException
	{
		// Generate some data
		EntityGenerator entityGenerator = new EntityGenerator(10, true);
		DataSetGenerator dataSetGenerator = new DataSetGenerator(
				entityGenerator);

		int numNodes = 30;
		int numWays = 20;
		int numRelations = 10;

		TestDataSet data = dataSetGenerator.generate(numNodes, numWays,
				numRelations);

		// Write the data set to XML

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OsmXmlOutputStream output = new OsmXmlOutputStream(baos, true);
		DataSetHelper.write(data, output);
		output.complete();

		String xml = new String(baos.toByteArray());

		// Re-read the data from XML

		InputStream input = IOUtils.toInputStream(xml);
		OsmXmlIterator iterator = new OsmXmlIterator(input, true);
		TestDataSet reread = DataSetHelper.read(iterator);

		// Compare generated data set to re-read data
		Assert.assertTrue("generated data vs. written and re-read",
				DataSetHelper.equals(data, reread));
	}

}

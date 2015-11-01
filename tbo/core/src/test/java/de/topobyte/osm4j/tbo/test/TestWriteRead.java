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

package de.topobyte.osm4j.tbo.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.testing.DataSet;
import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.EntityGenerator;

public class TestWriteRead
{

	private File file;

	@Before
	public void createTestFile() throws IOException
	{
		file = File.createTempFile("test", ".tbo");
	}

	@After
	public void deleteTestFile()
	{
		file.delete();
	}

	@Test
	public void test() throws IOException
	{
		// Generate some data
		EntityGenerator entityGenerator = new EntityGenerator(100, true);
		DataSetGenerator dataSetGenerator = new DataSetGenerator(
				entityGenerator);
		DataSet generated = dataSetGenerator.generate(10, 3, 2);

		// Write to file
		OutputStream output = new FileOutputStream(file);
		OsmOutputStream osmOutput = new TboWriter(output, true);
		DataSetHelper.write(generated, osmOutput);
		osmOutput.complete();
		output.close();

		// Read from file
		InputStream input = new FileInputStream(file);
		OsmIterator iterator = new TboIterator(input);
		InMemoryDataSet data = DataSetReader.read(iterator, true, true, true);
		DataSet read = new DataSet(data);

		// Compare data
		Assert.assertTrue(DataSetHelper.equals(generated, read));
		Assert.assertTrue(DataSetHelper.nodesEqual(generated.getNodes(),
				read.getNodes()));
		Assert.assertTrue(DataSetHelper.waysEqual(generated.getWays(),
				read.getWays()));
		Assert.assertTrue(DataSetHelper.relationsEqual(
				generated.getRelations(), read.getRelations()));
	}

}

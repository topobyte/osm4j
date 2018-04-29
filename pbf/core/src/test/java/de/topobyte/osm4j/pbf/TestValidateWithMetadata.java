// Copyright 2018 Sebastian Kuerten
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

package de.topobyte.osm4j.pbf;

import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.model.TestMetadata;

public class TestValidateWithMetadata extends TestValidateData
{

	public TestValidateWithMetadata()
	{
		super("data-with-metadata.pbf", "data-with-metadata.osm", true);
	}

	@Test
	public void testIterator() throws IOException
	{
		validateUsingIterator();
	}

	@Test
	public void testReader() throws IOException, OsmInputException
	{
		validateUsingReader();
	}

	@Test
	public void test() throws IOException, OsmInputException
	{
		TestDataSet xmlData = xmlData(resourceXML, fetchMetadata);

		PbfReader reader = Util.reader(resourcePBF, fetchMetadata);
		TestDataSet pbfData = DataSetHelper.read(reader);

		TestMetadata m1 = xmlData.getNodes().get(0).getMetadata();
		TestMetadata m2 = pbfData.getNodes().get(0).getMetadata();

		// It seems we're not handling timestamps correctly in the PBF layer
		System.out.println("node id: " + xmlData.getNodes().get(0).getId());
		System.out.println("xml date: " + new Date(m1.getTimestamp()));
		System.out.println("pbf date: " + new Date(m2.getTimestamp()));
		long diff = m1.getTimestamp() - m2.getTimestamp();
		System.out.println("millisecond diff: " + diff);
		System.out.println("hour diff: " + diff / 1000 / 60 / 60);
	}

}

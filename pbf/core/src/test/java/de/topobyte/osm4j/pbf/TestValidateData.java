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
import java.io.InputStream;

import org.junit.Assert;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class TestValidateData
{

	protected String resourcePBF;
	protected String resourceXML;
	protected boolean fetchMetadata;

	public TestValidateData(String resourcePBF, String resourceXML,
			boolean fetchMetadata)
	{
		this.resourcePBF = resourcePBF;
		this.resourceXML = resourceXML;
		this.fetchMetadata = fetchMetadata;
	}

	protected TestDataSet xmlData(String resource, boolean fetchMetadata)
			throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);
		OsmIterator iterator = new OsmXmlIterator(input, fetchMetadata);
		TestDataSet data = DataSetHelper.read(iterator);
		return data;
	}

	protected void validateUsingIterator() throws IOException
	{
		TestDataSet xmlData = xmlData(resourceXML, fetchMetadata);

		OsmIterator iterator = Util.iterator(resourcePBF, fetchMetadata);
		TestDataSet pbfData = DataSetHelper.read(iterator);

		boolean equals = DataSetHelper.equals(xmlData, pbfData);
		Assert.assertTrue("Comparison with XML data", equals);
	}

	protected void validateUsingReader() throws IOException, OsmInputException
	{
		TestDataSet xmlData = xmlData(resourceXML, fetchMetadata);

		PbfReader reader = Util.reader(resourcePBF, fetchMetadata);
		TestDataSet pbfData = DataSetHelper.read(reader);

		boolean equals = DataSetHelper.equals(xmlData, pbfData);
		Assert.assertTrue("Comparison with XML data", equals);
	}

}

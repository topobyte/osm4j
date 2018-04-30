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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.testing.model.TestMetadata;

public class TestValidateSingleNode extends TestValidateData
{

	public TestValidateSingleNode()
	{
		super("node20246140.pbf", "node20246140.osm", true);
	}

	@Test
	public void test() throws IOException, OsmInputException
	{
		TestDataSet xmlData = xmlData(resourceXML, fetchMetadata);

		PbfReader reader = Util.reader(resourcePBF, fetchMetadata);
		TestDataSet pbfData = DataSetHelper.read(reader);

		TestMetadata m1 = xmlData.getNodes().get(0).getMetadata();
		TestMetadata m2 = pbfData.getNodes().get(0).getMetadata();

		Assert.assertEquals("timestamp XMl vs. PBF", m1.getTimestamp(),
				m2.getTimestamp());

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();
		String formatted = formatter.print(m2.getTimestamp());
		Assert.assertEquals("PBF interpreted timestamp", "2013-09-22T09:04:42Z",
				formatted);
	}

}

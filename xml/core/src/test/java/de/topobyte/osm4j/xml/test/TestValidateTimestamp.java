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

package de.topobyte.osm4j.xml.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class TestValidateTimestamp
{

	@Test
	public void test() throws IOException
	{
		String filename = "node-240109189.osm";

		String text = Util.read(filename);

		// Parse the input

		InputStream input = IOUtils.toInputStream(text, "UTF-8");

		OsmXmlIterator iterator = new OsmXmlIterator(input, true);
		OsmNode node = (OsmNode) iterator.next().getEntity();

		// Check timestamp and reformatted timestamp

		long timestamp = node.getMetadata().getTimestamp();
		Assert.assertEquals("timestamp", 1494529484000L, timestamp);

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZoneUTC();

		String expected = "2017-05-11T19:04:44Z";
		String formatted = formatter.print(timestamp);
		Assert.assertEquals("formatted timestamp", expected, formatted);
	}

}

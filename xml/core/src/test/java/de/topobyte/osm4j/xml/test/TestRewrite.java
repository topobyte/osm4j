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

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class TestRewrite
{

	@Test
	public void test() throws IOException
	{
		String filename = "node-240109189.osm";

		ClassLoader classloader = TestRewriteNoMeta.class.getClassLoader();

		// Read the input to a string

		InputStream input = classloader.getResourceAsStream(filename);
		String text = IOUtils.toString(input);
		input.close();

		// Parse the input and rewrite to XML

		input = IOUtils.toInputStream(text, "UTF-8");

		OsmXmlIterator iterator = new OsmXmlIterator(input, true);
		OsmNode node = (OsmNode) iterator.next().getEntity();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OsmXmlOutputStream output = new OsmXmlOutputStream(baos, true);
		output.write(node);
		output.complete();

		String rewritten = new String(baos.toByteArray());

		// Compare original and rewritten text

		Assert.assertEquals("original vs. rewritten", text, rewritten);
	}

}

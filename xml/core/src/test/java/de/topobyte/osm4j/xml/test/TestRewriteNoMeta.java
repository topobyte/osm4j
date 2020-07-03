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
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class TestRewriteNoMeta extends LocaleTest
{

	public TestRewriteNoMeta(Locale locale)
	{
		super(locale);
	}

	@Test
	public void test() throws IOException
	{
		String filenameWithMeta = "node-240109189.osm";
		String filenameWithoutMeta = "node-240109189-nometa.osm";

		String text = Util.read(filenameWithoutMeta);

		// Parse the input and rewrite to XML

		InputStream input = Util.stream(filenameWithMeta);

		OsmXmlIterator iterator = new OsmXmlIterator(input, true);
		OsmNode node = (OsmNode) iterator.next().getEntity();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OsmXmlOutputStream output = new OsmXmlOutputStream(baos, false);
		output.write(node);
		output.complete();

		String rewritten = new String(baos.toByteArray());

		// Compare to expected result

		Assert.assertEquals("original vs. rewritten", text, rewritten);
	}

}

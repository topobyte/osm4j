// Copyright 2019 Sebastian Kuerten
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

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.osc.CompletenessChecker;
import de.topobyte.osm4j.osc.dynsax.OsmOscReader;

public class TestCompletenessChecker
{

	@Test
	public void test() throws IOException, OsmInputException
	{
		String filename = "003-338-100.osc.gz";

		InputStream cinput = Util.stream(filename);
		InputStream input = new GzipCompressorInputStream(cinput);

		CompletenessChecker task = new CompletenessChecker();

		OsmOscReader reader = new OsmOscReader(input, true);
		reader.setHandler(task);
		reader.read();
	}

}

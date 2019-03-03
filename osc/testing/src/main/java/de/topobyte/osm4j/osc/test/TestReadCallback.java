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

package de.topobyte.osm4j.osc.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.osc.OsmChange;
import de.topobyte.osm4j.osc.dynsax.OsmChangeHandler;
import de.topobyte.osm4j.osc.dynsax.OsmOscReader;

public class TestReadCallback implements OsmChangeHandler
{

	public static void main(String[] args)
			throws ParserConfigurationException, OsmInputException, IOException
	{
		if (args.length != 1) {
			System.out
					.println("usage: " + TestReadCallback.class.getSimpleName()
							+ " <input osm osc>");
			System.exit(1);
		}

		String pathInput = args[0];

		Path path = Paths.get(pathInput);
		InputStream cinput = Files.newInputStream(path);
		InputStream input = new GzipCompressorInputStream(cinput);

		TestReadCallback test = new TestReadCallback();
		OsmOscReader reader = new OsmOscReader(input, false);

		reader.setHandler(test);
		reader.read();
	}

	@Override
	public void complete() throws IOException
	{
		System.out.println("complete");
	}

	@Override
	public void handle(OsmChange change) throws IOException
	{
		System.out.println("change: " + change.getType() + " "
				+ change.getElements().size());
	}

}

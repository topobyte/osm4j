// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.core.resolve;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.topobyte.osm4j.core.test.ListLoader;
import de.topobyte.osm4j.core.test.Loader;
import de.topobyte.osm4j.core.test.MapLoader;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

@RunWith(Parameterized.class)
public abstract class BaseTestDataSets
{

	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data()
	{
		return Arrays.asList(new Object[][] { { new MapLoader() },
				{ new ListLoader() } });
	}

	private Loader loader;
	protected OsmEntityProvider data;

	public BaseTestDataSets(Loader loader)
	{
		this.loader = loader;
	}

	@Before
	public void prepare() throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("data1.osm");
		OsmXmlIterator iterator = new OsmXmlIterator(input, false);
		data = loader.load(iterator);
		input.close();
	}

}

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

import org.apache.commons.io.IOUtils;

public class Util
{

	public static String read(String filename) throws IOException
	{
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		InputStream input = classloader.getResourceAsStream(filename);
		String text = IOUtils.toString(input);
		input.close();

		return text;
	}

	public static InputStream stream(String filename)
	{
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		return classloader.getResourceAsStream(filename);
	}

}

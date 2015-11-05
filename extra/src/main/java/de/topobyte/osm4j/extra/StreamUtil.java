// Copyright 2015 Sebastian Kuerten
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

package de.topobyte.osm4j.extra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil
{

	public static OutputStream bufferedOutputStream(File file)
			throws FileNotFoundException
	{
		OutputStream out = new FileOutputStream(file);
		return new BufferedOutputStream(out);
	}

	public static OutputStream bufferedOutputStream(String path)
			throws FileNotFoundException
	{
		OutputStream out = new FileOutputStream(path);
		return new BufferedOutputStream(out);
	}

	public static InputStream bufferedInputStream(File file)
			throws FileNotFoundException
	{
		InputStream in = new FileInputStream(file);
		return new BufferedInputStream(in);
	}

	public static InputStream bufferedInputStream(String path)
			throws FileNotFoundException
	{
		InputStream in = new FileInputStream(path);
		return new BufferedInputStream(in);
	}

}

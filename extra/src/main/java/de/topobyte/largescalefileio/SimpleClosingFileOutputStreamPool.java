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

package de.topobyte.largescalefileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SimpleClosingFileOutputStreamPool implements
		ClosingFileOutputStreamPool
{

	private OutputStream cache = null;
	private int cacheId = -1;

	@Override
	public OutputStream create(File file, int id, boolean append)
			throws IOException
	{
		if (cache == null) {
			cache = new FileOutputStream(file, append);
			cacheId = id;
			return cache;
		} else if (cacheId == id) {
			return cache;
		} else {
			cache.close();
			cache = new FileOutputStream(file, append);
			cacheId = id;
			return cache;
		}
	}

	@Override
	public void flush(int id) throws IOException
	{
		if (id == cacheId) {
			cache.flush();
		}
	}

	@Override
	public void close(int id) throws IOException
	{
		if (id == cacheId) {
			cache.close();
			cache = null;
			cacheId = -1;
		}
	}

}

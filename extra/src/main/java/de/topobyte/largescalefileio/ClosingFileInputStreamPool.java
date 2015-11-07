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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClosingFileInputStreamPool implements
		ClosingFileInputStreamFactory
{

	private InputStream cache = null;
	private int cacheId = -1;

	@Override
	public InputStream create(File file, int id, long pos) throws IOException
	{
		if (cache == null) {
			cache = new FileInputStream(file);
			seek(pos);
			cacheId = id;
			return cache;
		} else if (cacheId == id) {
			return cache;
		} else {
			cache.close();
			cache = new FileInputStream(file);
			seek(pos);
			cacheId = id;
			return cache;
		}
	}

	private void seek(long pos) throws IOException
	{
		long remaining = pos;
		while (remaining > 0) {
			long s = cache.skip(remaining);
			if (s < 0) {
				throw new IOException();
			}
			remaining -= s;
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

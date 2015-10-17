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

package de.topobyte.osm4j.tbo.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamCompactReader extends CompactReader
{

	private final InputStream is;

	public InputStreamCompactReader(InputStream is)
	{
		super();
		this.is = is;
	}

	@Override
	public int readByte() throws IOException
	{
		int r = is.read();
		if (r < 0) {
			throw new EOFException();
		}
		return r;
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException
	{
		int r = is.read(buffer, off, len);
		if (r < 0) {
			throw new EOFException();
		}
		return r;
	}

}

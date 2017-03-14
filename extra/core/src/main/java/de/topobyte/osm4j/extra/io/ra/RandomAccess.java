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

package de.topobyte.osm4j.extra.io.ra;

import java.io.Closeable;
import java.io.IOException;

public interface RandomAccess extends Closeable
{

	public void seek(long pos) throws IOException;

	public long getFilePointer() throws IOException;

	public short readShort() throws IOException;

	public int readInt() throws IOException;

	public long readLong() throws IOException;

	public float readFloat() throws IOException;

	public double readDouble() throws IOException;

	public int read(byte b[]) throws IOException;

	public int read(byte b[], int off, int len) throws IOException;

}

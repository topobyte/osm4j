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

package de.topobyte.osm4j.extra.datatree.closingstreams;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayWriter
{

	private byte[] buffer;
	private OutputStream output;
	private int pos = 0;

	public ByteArrayWriter(byte[] buffer, OutputStream output)
	{
		this.buffer = buffer;
		this.output = output;
	}

	public boolean done()
	{
		return pos >= buffer.length;
	}

	public int remaining()
	{
		return buffer.length - pos;
	}

	public void writeByte() throws IOException
	{
		output.write(buffer[pos++]);
	}

	public void writeBytes(int n) throws IOException
	{
		byte[] bytes = new byte[n];
		System.arraycopy(buffer, pos, bytes, 0, n);
		output.write(bytes);
		pos += n;
	}

	public void writeBytes(int n, int paddingStart, int paddingEnd)
			throws IOException
	{
		byte[] bytes = new byte[n + paddingStart + paddingEnd];
		System.arraycopy(buffer, pos, bytes, paddingStart, n);
		output.write(bytes, paddingStart, n);
		pos += n;
	}

}

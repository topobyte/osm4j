// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A wrapper for InputStreams that provides methods similar to the ones found in
 * RandomAccessFile to read primitives directly.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class HighLevelInputStream implements Closeable
{

	private DataInputStream dis;

	/**
	 * Create a new high level wrapper for the given stream.
	 * 
	 * @param is
	 *            the stream to wrap.
	 */
	public HighLevelInputStream(InputStream is)
	{
		dis = new DataInputStream(is);
	}

	/**
	 * Read a byte array.
	 * 
	 * @param bytes
	 *            the array to read into.
	 * @throws IOException
	 */
	public void read(byte[] bytes) throws IOException
	{
		dis.readFully(bytes);
	}

	/**
	 * Read a short value.
	 * 
	 * @return the short
	 * @throws IOException
	 *             on reading failure
	 */
	public final short readShort() throws IOException
	{
		return dis.readShort();
	}

	/**
	 * Read an integer.
	 * 
	 * @return the integer.
	 * @throws IOException
	 *             on reading failure.
	 */
	public final int readInt() throws IOException
	{
		return dis.readInt();
	}

	/**
	 * Read a long.
	 * 
	 * @return the long.
	 * @throws IOException
	 *             on reading failure.
	 */
	public final long readLong() throws IOException
	{
		return dis.readLong();
	}

	/**
	 * Read a double.
	 * 
	 * @return the double.
	 * @throws IOException
	 *             on reading failure.
	 */
	public final double readDouble() throws IOException
	{
		return dis.readDouble();
	}

	/**
	 * Write this string to the stream.
	 * 
	 * @throws IOException
	 *             on io failure.
	 * 
	 * @return the read string.
	 */
	public final String readString() throws IOException
	{
		int nbytes = readShort();
		byte[] bytes = new byte[nbytes];
		dis.readFully(bytes);
		return new String(bytes, "UTF-8");
	}

	@Override
	public void close() throws IOException
	{
		dis.close();
	}

}

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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A wrapper for OutputStreams that provides methods similar to the ones found
 * in RandomAccessFile to write primitives directly.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class HighLevelOutputStream implements Closeable
{

	private DataOutputStream dos;

	/**
	 * Create a new high level wrapper for the given stream.
	 * 
	 * @param os
	 *            the stream to wrap.
	 */
	public HighLevelOutputStream(OutputStream os)
	{
		dos = new DataOutputStream(os);
	}

	/**
	 * Write a byte array
	 * 
	 * @param v
	 *            the bytes to write.
	 * @throws IOException
	 *             on writing failure
	 */
	public void write(byte v[]) throws IOException
	{
		dos.write(v);
	}

	/**
	 * Write a byte.
	 * 
	 * @param v
	 *            the byte to write.
	 * @throws IOException
	 *             on writing failure
	 */
	public final void writeByte(int v) throws IOException
	{
		dos.write(v);
	}

	/**
	 * Write a short.
	 * 
	 * @param v
	 *            the short to write.
	 * @throws IOException
	 *             on writing failure
	 */
	public final void writeShort(int v) throws IOException
	{
		dos.writeShort(v);
	}

	/**
	 * Write a long
	 * 
	 * @param v
	 *            the long to write.
	 * @throws IOException
	 *             on writing failure
	 */
	public final void writeLong(long v) throws IOException
	{
		dos.writeLong(v);
	}

	/**
	 * Write a double.
	 * 
	 * @param v
	 *            the double to write.
	 * @throws IOException
	 *             on writing failure
	 */
	public final void writeDouble(double v) throws IOException
	{
		dos.writeDouble(v);
	}

	/**
	 * Write this string to the stream.
	 * 
	 * @param string
	 *            the string to write
	 * @throws IOException
	 *             on io failure.
	 */
	public final void writeString(String string) throws IOException
	{
		byte[] bytes = string.getBytes("UTF-8");
		writeShort(bytes.length);
		dos.write(bytes);
	}

	@Override
	public void close() throws IOException
	{
		dos.close();
	}

}

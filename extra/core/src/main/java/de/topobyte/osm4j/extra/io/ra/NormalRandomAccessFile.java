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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NormalRandomAccessFile implements RandomAccess
{

	private FileChannel channel;

	public NormalRandomAccessFile(Path file) throws IOException
	{
		channel = FileChannel.open(file, StandardOpenOption.READ);
	}

	public NormalRandomAccessFile(String name) throws IOException
	{
		this(Paths.get(name));
	}

	@Override
	public void close() throws IOException
	{
		channel.close();
	}

	@Override
	public void seek(long pos) throws IOException
	{
		channel.position(pos);
	}

	@Override
	public long getFilePointer() throws IOException
	{
		return channel.position();
	}

	private void readFully(ByteBuffer buffer) throws IOException
	{
		while (buffer.hasRemaining()) {
			int read = channel.read(buffer);
			if (read < 0) {
				break;
			}
		}
	}

	@Override
	public short readShort() throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(2);
		readFully(buffer);
		buffer.position(0);
		return buffer.getShort();
	}

	@Override
	public int readInt() throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		readFully(buffer);
		buffer.position(0);
		return buffer.getInt();
	}

	@Override
	public long readLong() throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		readFully(buffer);
		buffer.position(0);
		return buffer.getLong();
	}

	@Override
	public float readFloat() throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		readFully(buffer);
		buffer.position(0);
		return buffer.getFloat();
	}

	@Override
	public double readDouble() throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		readFully(buffer);
		buffer.position(0);
		return buffer.getDouble();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.wrap(b);
		return channel.read(buffer);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(len);
		int read = channel.read(buffer);
		buffer.position(0);
		buffer.get(b, off, read);
		return read;
	}

}

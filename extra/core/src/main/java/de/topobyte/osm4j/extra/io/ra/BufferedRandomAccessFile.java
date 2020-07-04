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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class BufferedRandomAccessFile implements RandomAccess
{

	private FileChannel channel;
	private long fileSize;

	private int pageSize;
	private int cacheSize; // in number of pages
	private long filePointer = 0;

	private HashMap<Long, Page> pages;

	public BufferedRandomAccessFile(Path file, int pageSize, int cacheSize)
			throws IOException
	{
		channel = FileChannel.open(file, StandardOpenOption.READ);
		this.pageSize = pageSize;
		this.cacheSize = cacheSize;

		fileSize = Files.size(file);
		pages = new LruHashMap<>(cacheSize);
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public int getCacheSize()
	{
		return cacheSize;
	}

	@Override
	public void close() throws IOException
	{
		channel.close();
	}

	@Override
	public void seek(long pos) throws IOException
	{
		filePointer = pos;
	}

	@Override
	public long getFilePointer()
	{
		return filePointer;
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

	public byte readByte() throws IOException
	{
		long pageNumber = filePointer / pageSize;
		int pageOffset = (int) (filePointer % pageSize);
		Page page = getPage(pageNumber);
		byte[] data = page.getData();
		if (pageOffset >= data.length) {
			throw new EOFException();
		}
		filePointer++;
		return data[pageOffset];
	}

	private Page getPage(long pageNumber) throws IOException
	{
		Page page = pages.get(pageNumber);
		if (page == null) {
			page = readPage(pageNumber);
			pages.put(pageNumber, page);
		}
		return page;
	}

	private Page readPage(long pageNumber) throws IOException
	{
		long pageOffset = pageNumber * pageSize;
		int size = (int) Math.min(pageSize, fileSize - pageOffset);
		byte[] buffer = new byte[size];
		channel.position(pageOffset);
		ByteBuffer buf = ByteBuffer.wrap(buffer);
		readFully(buf);
		Page page = new Page(pageOffset, buffer);
		return page;
	}

	@Override
	public short readShort() throws IOException
	{
		int b1 = (readByte()) & 0xFF;
		int b2 = (readByte()) & 0xFF;
		return (short) ((b1 << 8) | b2);
	}

	@Override
	public int readInt() throws IOException
	{
		int b1 = (readByte()) & 0xFF;
		int b2 = (readByte()) & 0xFF;
		int b3 = (readByte()) & 0xFF;
		int b4 = (readByte()) & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	@Override
	public long readLong() throws IOException
	{
		long i1 = (readInt()) & 0xFFFFFFFFL;
		long i2 = (readInt()) & 0xFFFFFFFFL;
		return (i1 << 32) | i2;
	}

	@Override
	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public int read(byte b[]) throws IOException
	{
		return readBytes(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		return readBytes(b, off, len);
	}

	private int readBytes(byte[] b, int off, int len) throws IOException
	{
		long pageNumber = filePointer / pageSize;
		int pageOffset = (int) (filePointer % pageSize);
		Page page = getPage(pageNumber);

		int inPage = page.getData().length - pageOffset;
		int get = Math.min(inPage, len);
		if (get == 0) {
			throw new EOFException();
		}
		filePointer += get;
		System.arraycopy(page.getData(), pageOffset, b, off, get);
		return get;
	}

}

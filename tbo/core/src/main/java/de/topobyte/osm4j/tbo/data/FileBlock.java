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

package de.topobyte.osm4j.tbo.data;

import de.topobyte.osm4j.tbo.Compression;

public class FileBlock
{

	private final int type;
	private final Compression compression;
	private final int uncompressedLength;
	private final int numObjects;
	private final byte[] buffer;
	private final int length;

	public FileBlock(int type, Compression compression, int uncompressedLength,
			int numObjects, byte[] buffer, int length)
	{
		this.type = type;
		this.compression = compression;
		this.uncompressedLength = uncompressedLength;
		this.numObjects = numObjects;
		this.buffer = buffer;
		this.length = length;
	}

	public int getType()
	{
		return type;
	}

	public Compression getCompression()
	{
		return compression;
	}

	public int getUncompressedLength()
	{
		return uncompressedLength;
	}

	public int getNumObjects()
	{
		return numObjects;
	}

	public byte[] getBuffer()
	{
		return buffer;
	}

	public int getLength()
	{
		return length;
	}

}

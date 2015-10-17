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

package de.topobyte.osm4j.tbo.access;

import java.io.EOFException;
import java.io.IOException;

import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.io.CompactReader;

public class BlockReader
{

	private final CompactReader reader;

	public BlockReader(CompactReader reader)
	{
		this.reader = reader;
	}

	public FileBlock readBlock() throws IOException
	{
		int typeByte = 0;
		try {
			typeByte = reader.readByte();
		} catch (EOFException e) {
			return null;
		}
		int numBytes = (int) reader.readVariableLengthSignedInteger();
		int numObjects = (int) reader.readVariableLengthSignedInteger();

		byte[] buffer = new byte[numBytes];
		reader.readFully(buffer);

		return new FileBlock(typeByte, numObjects, buffer);
	}

}

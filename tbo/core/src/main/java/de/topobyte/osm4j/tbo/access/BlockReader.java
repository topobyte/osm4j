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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.InputStreamCompactReader;
import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;

public class BlockReader
{

	protected final CompactReader reader;

	private static TIntObjectMap<Compression> compressions = new TIntObjectHashMap<>();
	static {
		for (Compression compression : Compression.values()) {
			compressions.put(compression.getId(), compression);
		}
	}

	public BlockReader(InputStream input)
	{
		this(new InputStreamCompactReader(input));
	}

	public BlockReader(CompactReader reader)
	{
		this.reader = reader;
	}

	public FileHeader parseHeader() throws IOException
	{
		return ReaderUtil.parseHeader(reader);
	}

	public FileBlock readBlock() throws IOException
	{
		// Type of the block
		int typeByte = 0;
		try {
			typeByte = reader.readByte();
		} catch (EOFException e) {
			return null;
		}

		// Length of the chunk
		int length = (int) reader.readVariableLengthUnsignedInteger();
		// Keep track of the length of meta data to determine the data size
		int lengthMeta = 0;

		int compressionByte = reader.readByte();
		lengthMeta += 1;
		Compression compression = compressions.get(compressionByte);
		if (compression == null) {
			throw new IOException("Unsupported compression method");
		}

		// The uncompressed length is either stored here, because data is
		// actually compressed
		int uncompressedLength = 0;
		if (compression != Compression.NONE) {
			uncompressedLength = (int) reader
					.readVariableLengthUnsignedInteger();
			lengthMeta += CompactWriter
					.getNumberOfBytesUnsigned(uncompressedLength);
		}

		int numObjects = (int) reader.readVariableLengthUnsignedInteger();
		lengthMeta += CompactWriter.getNumberOfBytesUnsigned(numObjects);

		// Now we can calculate the length of the data part
		int compressedLength = length - lengthMeta;

		// In case of no compression, the uncompressed length is the same as the
		// compressed length
		if (compression == Compression.NONE) {
			uncompressedLength = compressedLength;
		}

		byte[] buffer = new byte[compressedLength];
		reader.readFully(buffer);

		return new FileBlock(typeByte, compression, uncompressedLength,
				numObjects, buffer, compressedLength);
	}

}

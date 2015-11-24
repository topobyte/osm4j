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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;

public class DefaultBlockWriter implements BlockWriter
{

	protected final CompactWriter writer;

	public DefaultBlockWriter(CompactWriter writer)
	{
		this.writer = writer;
	}

	@Override
	public void writeHeader(FileHeader header) throws IOException
	{
		header.write(writer);
	}

	@Override
	public void writeBlock(FileBlock block) throws IOException
	{
		// Determine the total block length (meta info + data, excluding type
		// byte and the length field)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeBlockInfo(new OutputStreamCompactWriter(baos), block);
		int total = baos.size() + block.getLength();

		// Type of the block
		writer.writeByte(block.getType());
		// Length of chunk
		writer.writeVariableLengthUnsignedInteger(total);

		// meta info
		writeBlockInfo(writer, block);
		// data
		writer.write(block.getBuffer(), 0, block.getLength());
	}

	private void writeBlockInfo(CompactWriter writer, FileBlock block)
			throws IOException
	{
		writer.writeByte(block.getCompression().getId());
		if (block.getCompression() != Compression.NONE) {
			writer.writeVariableLengthUnsignedInteger(block
					.getUncompressedLength());
		}
		writer.writeVariableLengthUnsignedInteger(block.getNumObjects());
	}

}

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

import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.io.CompactWriter;
import de.topobyte.osm4j.tbo.io.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.writerhelper.Blockable;

public class BlockWriter
{

	protected final CompactWriter writer;

	private ByteArrayOutputStream baos;

	private boolean lowMemoryFootprint;

	public BlockWriter(CompactWriter writer)
	{
		this(writer, false);
	}

	public BlockWriter(CompactWriter writer, boolean lowMemoryFootprint)
	{
		this.writer = writer;
		this.lowMemoryFootprint = lowMemoryFootprint;
		if (!lowMemoryFootprint) {
			baos = new ByteArrayOutputStream(1024 * 1024 * 16);
		}
	}

	public void writeBlock(FileBlock block) throws IOException
	{
		writer.writeByte(block.getType());
		writer.writeVariableLengthSignedInteger(block.getBuffer().length);
		writer.writeVariableLengthSignedInteger(block.getNumObjects());
		writer.write(block.getBuffer());
	}

	public void writeBlock(Blockable blockable, int type, int count)
			throws IOException
	{
		/* with compression */
		// baos.reset();
		// GZIPOutputStream out = new GZIPOutputStream(baos);
		// // DeflaterOutputStream out = new DeflaterOutputStream(baos);
		// OutputStreamCompactWriter bufferWriter = new
		// OutputStreamCompactWriter(
		// out);
		// blockable.write(bufferWriter);
		// out.close();

		/* without compression */
		if (lowMemoryFootprint) {
			baos = new ByteArrayOutputStream(1024 * 1024 * 16);
		} else {
			baos.reset();
		}

		OutputStreamCompactWriter bufferWriter = new OutputStreamCompactWriter(
				baos);
		blockable.write(bufferWriter);

		/* common code */
		byte[] bytes = baos.toByteArray();
		writer.writeByte(type);
		writer.writeVariableLengthSignedInteger(bytes.length);
		writer.writeVariableLengthSignedInteger(count);
		writer.write(bytes);

		if (lowMemoryFootprint) {
			baos = null;
		}
	}

}

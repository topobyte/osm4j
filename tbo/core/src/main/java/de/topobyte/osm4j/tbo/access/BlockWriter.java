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
import java.util.zip.DeflaterOutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.data.FileBlock;
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
			baos = new ByteArrayOutputStream();
		}
	}

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

	public void writeBlock(Blockable blockable, int type, int count,
			Compression compression) throws IOException
	{
		if (lowMemoryFootprint) {
			baos = new ByteArrayOutputStream();
		} else {
			baos.reset();
		}

		CompactWriter bufferWriter = new OutputStreamCompactWriter(baos);
		blockable.write(bufferWriter);
		byte[] uncompressed = baos.toByteArray();
		byte[] compressed = null;
		int length = 0;
		baos.reset();

		switch (compression) {
		default:
		case NONE:
			compressed = uncompressed;
			length = compressed.length;
			break;
		case DEFLATE:
			DeflaterOutputStream out = new DeflaterOutputStream(baos);
			out.write(uncompressed);
			out.close();
			compressed = baos.toByteArray();
			length = compressed.length;
			break;
		case LZ4:
			initLz4();
			int estimate = lz4Compressor
					.maxCompressedLength(uncompressed.length);
			compressed = new byte[estimate];
			length = lz4Compressor.compress(uncompressed, compressed);
			break;
		}

		FileBlock block = new FileBlock(type, compression, uncompressed.length,
				count, compressed, length);
		writeBlock(block);

		if (lowMemoryFootprint) {
			baos = null;
		}
	}

	private LZ4Compressor lz4Compressor = null;

	private void initLz4()
	{
		if (lz4Compressor == null) {
			LZ4Factory factory = LZ4Factory.fastestInstance();
			lz4Compressor = factory.fastCompressor();
		}
	}

}

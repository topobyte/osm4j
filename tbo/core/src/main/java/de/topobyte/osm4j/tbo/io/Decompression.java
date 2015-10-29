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

package de.topobyte.osm4j.tbo.io;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import de.topobyte.osm4j.tbo.data.FileBlock;

public class Decompression
{

	public static byte[] decompress(FileBlock block) throws IOException
	{
		byte[] compressed = block.getBuffer();
		byte[] uncompressed;
		switch (block.getCompression()) {
		default:
		case NONE:
			uncompressed = compressed;
			break;
		case DEFLATE:
			uncompressed = new byte[block.getUncompressedLength()];

			Inflater decompresser = new Inflater();
			decompresser.setInput(compressed);
			try {
				decompresser.inflate(uncompressed);
			} catch (DataFormatException e) {
				throw new IOException("Error while decompressing gzipped data",
						e);
			}
			decompresser.end();
			break;
		case LZ4:
			uncompressed = new byte[block.getUncompressedLength()];

			initLz4();
			lz4Decompressor.decompress(compressed, uncompressed);
			break;
		}
		return uncompressed;
	}

	private static LZ4FastDecompressor lz4Decompressor = null;

	private static void initLz4()
	{
		if (lz4Decompressor == null) {
			LZ4Factory factory = LZ4Factory.fastestInstance();
			lz4Decompressor = factory.fastDecompressor();
		}
	}

}

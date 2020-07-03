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

package de.topobyte.osm4j.tbo.executables;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.access.BlockReader;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;

public class BlockSizeInfo extends BlockReader
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + BlockSizeInfo.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);

		BlockSizeInfo task = new BlockSizeInfo(input);
		task.parse();
	}

	private static final String patternUncompressed = "Block %d [%s] (uncompressed): raw size: %d";
	private static final String patternDeflate = "Block %d [%s] (deflate): raw size: %d, compressed: %d";
	private static final String patternLz4 = "Block %d [%s] (lz4): raw size: %d, compressed: %d";

	private long nBlocks = 0;

	public BlockSizeInfo(InputStream input)
	{
		super(input);
	}

	public void parse() throws IOException
	{
		parseHeader();

		while (true) {
			FileBlock block = readBlock();
			if (block == null) {
				break;
			}

			String type = "unknown";
			switch (block.getType()) {
			case Definitions.BLOCK_TYPE_NODES:
				type = "nodes";
				break;
			case Definitions.BLOCK_TYPE_WAYS:
				type = "ways";
				break;
			case Definitions.BLOCK_TYPE_RELATIONS:
				type = "relations";
				break;
			}

			Compression compression = block.getCompression();
			if (compression == Compression.NONE) {
				System.out.println(String.format(patternUncompressed, nBlocks,
						type, block.getUncompressedLength()));
			} else if (compression == Compression.DEFLATE) {
				System.out.println(String.format(patternDeflate, nBlocks, type,
						block.getUncompressedLength(), block.getLength()));
			} else if (compression == Compression.LZ4) {
				System.out.println(String.format(patternLz4, nBlocks, type,
						block.getUncompressedLength(), block.getLength()));
			}

			nBlocks++;
		}
	}

}

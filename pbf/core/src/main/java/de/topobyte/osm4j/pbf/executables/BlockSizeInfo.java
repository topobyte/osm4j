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

package de.topobyte.osm4j.pbf.executables;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import crosby.binary.Fileformat.Blob;
import de.topobyte.osm4j.pbf.seq.BlobParser;
import de.topobyte.osm4j.pbf.util.BlobHeader;

public class BlockSizeInfo extends BlobParser
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + BlockSizeInfo.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);

		BlockSizeInfo task = new BlockSizeInfo();
		task.parse(input);

		task.finish();

	}

	private long nBlocks = 0;

	@Override
	protected void parse(BlobHeader header, Blob blob) throws IOException
	{
		int rawSize = blob.getRawSize();
		if (blob.hasRaw()) {
			System.out.println(String.format(
					"Block %d (uncompressed): raw size: %d", nBlocks, rawSize));
		} else if (blob.hasZlibData()) {
			int zlibSize = blob.getZlibData().size();
			System.out.println(String.format(
					"Block %d (gzip): raw size: %d, compressed: %d", nBlocks,
					rawSize, zlibSize));
		} else if (blob.hasLz4Data()) {
			int lz4Size = blob.getLz4Data().size();
			System.out.println(String.format(
					"Block %d (lz4): raw size: %d, compressed: %d", nBlocks,
					rawSize, lz4Size));
		}
		nBlocks++;
	}

	private void finish()
	{
		System.out.println("Number of blocks: " + nBlocks);
	}

}

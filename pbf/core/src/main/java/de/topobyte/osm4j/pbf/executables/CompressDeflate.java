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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import crosby.binary.Fileformat;
import de.topobyte.osm4j.pbf.Compression;
import de.topobyte.osm4j.pbf.seq.BlockWriter;
import de.topobyte.osm4j.pbf.util.BlobHeader;
import de.topobyte.osm4j.pbf.util.BlockData;
import de.topobyte.osm4j.pbf.util.PbfUtil;

public class CompressDeflate
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2) {
			System.out.println("usage: "
					+ CompressDeflate.class.getSimpleName()
					+ " <input> <output>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		OutputStream output = new FileOutputStream(args[1]);

		DataInputStream data = new DataInputStream(input);

		BlockWriter blockWriter = new BlockWriter(output);

		while (true) {
			try {
				BlobHeader header = PbfUtil.parseHeader(data);

				Fileformat.Blob blob = PbfUtil.parseBlock(data,
						header.getDataLength());

				BlockData blockData = PbfUtil.getBlockData(blob);

				blockWriter.write(header.getType(), null, Compression.DEFLATE,
						blockData.getBlobData());

			} catch (EOFException eof) {
				break;
			}
		}

		output.close();
	}

}

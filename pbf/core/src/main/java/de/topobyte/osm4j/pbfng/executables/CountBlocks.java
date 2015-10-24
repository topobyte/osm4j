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

package de.topobyte.osm4j.pbfng.executables;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.pbfng.util.BlockHeader;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public class CountBlocks
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + CountBlocks.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		DataInputStream data = new DataInputStream(input);

		long nBlocks = 0;

		while (true) {
			try {
				BlockHeader blockHeader = PbfUtil.parseHeader(data);
				input.skip(blockHeader.getDataLength());
				nBlocks++;
			} catch (EOFException eof) {
				break;
			}
		}

		System.out.println("Number of blocks: " + nBlocks);
	}

}

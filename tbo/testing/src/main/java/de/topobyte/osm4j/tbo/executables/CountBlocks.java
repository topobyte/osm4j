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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.tbo.access.BlockReader;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;

public class CountBlocks
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + CountBlocks.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		FileInputStream input = new FileInputStream(file);

		CountBlocks fastCount = new CountBlocks(input);
		fastCount.execute();
	}

	private long nc = 0, wc = 0, rc = 0;
	private BlockReader reader;

	public CountBlocks(InputStream input)
	{
		reader = new BlockReader(input);
	}

	private void execute() throws IOException
	{
		reader.parseHeader();

		while (true) {
			FileBlock block = reader.readBlock();
			if (block == null) {
				break;
			}
			switch (block.getType()) {
			case Definitions.BLOCK_TYPE_NODES:
				nc++;
				break;
			case Definitions.BLOCK_TYPE_WAYS:
				wc++;
				break;
			case Definitions.BLOCK_TYPE_RELATIONS:
				rc++;
				break;
			}
		}
		printNumbers();
	}

	public void printNumbers()
	{
		System.out.println("node blocks:     " + nc);
		System.out.println("way blocks:      " + wc);
		System.out.println("relation blocks: " + rc);
		System.out.println("total:           " + (nc + wc + rc));
	}

}

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

public class FastCount
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + FastCount.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		FileInputStream input = new FileInputStream(file);

		FastCount fastCount = new FastCount(input);
		fastCount.execute();
	}

	private long nc = 0, wc = 0, rc = 0;
	private BlockReader reader;

	public FastCount(InputStream input)
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
			int numObjects = block.getNumObjects();
			switch (block.getType()) {
			case Definitions.BLOCK_TYPE_NODES:
				nc += numObjects;
				break;
			case Definitions.BLOCK_TYPE_WAYS:
				wc += numObjects;
				break;
			case Definitions.BLOCK_TYPE_RELATIONS:
				rc += numObjects;
				break;
			}
		}
		printNumbers();
	}

	public void printNumbers()
	{
		System.out.println("nodes:     " + nc);
		System.out.println("ways:      " + wc);
		System.out.println("relations: " + rc);
	}

}

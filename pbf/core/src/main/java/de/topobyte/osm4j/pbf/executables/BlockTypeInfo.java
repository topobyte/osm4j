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
import java.util.Set;

import crosby.binary.Osmformat;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbf.seq.BlockParser;
import de.topobyte.osm4j.pbf.util.PbfMeta;

public class BlockTypeInfo extends BlockParser
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + BlockTypeInfo.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);

		BlockTypeInfo task = new BlockTypeInfo();
		task.parse(input);

		task.finish();

	}

	private long nBlocks = 0;

	@Override
	protected void parse(Osmformat.HeaderBlock block)
	{
		System.out.println("Header block: " + nBlocks);
		nBlocks++;
	}

	@Override
	protected void parse(Osmformat.PrimitiveBlock block)
	{
		if (PbfMeta.hasMixedContent(block)) {
			Set<EntityType> types = PbfMeta.getContentTypes(block);
			System.out.println(String.format("Mixed content block (%d): %s",
					nBlocks, types.toString()));
		}
		nBlocks++;
	}

	private void finish()
	{
		System.out.println("Number of blocks: " + nBlocks);
	}

}

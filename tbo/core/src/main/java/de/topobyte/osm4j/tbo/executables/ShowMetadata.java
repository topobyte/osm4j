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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import de.topobyte.osm4j.tbo.access.BlockReader;
import de.topobyte.osm4j.tbo.access.ReaderUtil;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.Metadata;
import de.topobyte.osm4j.tbo.io.CompactReader;
import de.topobyte.osm4j.tbo.io.InputStreamCompactReader;

public class ShowMetadata extends BlockReader
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + ShowMetadata.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		CompactReader reader = new InputStreamCompactReader(input);

		ShowMetadata task = new ShowMetadata(reader);
		task.parse();
	}

	public ShowMetadata(CompactReader reader)
	{
		super(reader);
	}

	private void parse() throws IOException
	{
		FileBlock block = readBlock();
		if (block.getType() == Definitions.BLOCK_TYPE_METADATA) {
			byte[] buffer = block.getBuffer();
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			CompactReader reader = new InputStreamCompactReader(bais);
			Metadata metadata = ReaderUtil.parseMetadata(reader);
			System.out.println("tbo version: " + metadata.getVersion());
			Map<String, String> tags = metadata.getTags();
			for (String key : tags.keySet()) {
				System.out.println(key + ": " + tags.get(key));
			}
		} else {
			System.out.println("First block is not a metadata block");
		}
	}

}

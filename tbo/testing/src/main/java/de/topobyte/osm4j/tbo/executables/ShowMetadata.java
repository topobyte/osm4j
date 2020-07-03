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
import java.util.Map;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.InputStreamCompactReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.tbo.access.BlockReader;
import de.topobyte.osm4j.tbo.access.ReaderUtil;
import de.topobyte.osm4j.tbo.data.FileHeader;

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

	private void parse()
	{
		FileHeader header;
		try {
			header = ReaderUtil.parseHeader(reader);
		} catch (IOException e) {
			System.out.println("Unable to parse file header");
			System.out.println("Error: " + e.getMessage());
			return;
		}

		System.out.println("tbo version: " + header.getVersion());
		Map<String, String> tags = header.getTags();
		if (tags.size() == 0) {
			System.out.println("no tags");
		} else {
			System.out.println("number of tags: " + tags.size());
			for (String key : tags.keySet()) {
				System.out.println(key + ": " + tags.get(key));
			}
		}
		System.out.println("has metadata: " + header.hasMetadata());
		if (!header.hasBounds()) {
			System.out.println("no bounds");
		} else {
			OsmBounds bounds = header.getBounds();
			System.out.println(String.format("Bounding box: %f,%f,%f,%f",
					bounds.getLeft(), bounds.getBottom(), bounds.getRight(),
					bounds.getTop()));
		}
	}

}

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

package de.topobyte.osm4j.tbo.access;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.InputStreamCompactReader;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.Decompression;

public class Reader extends BlockReader
{

	private boolean fetchTags;

	private boolean hasMetadata;
	private boolean fetchMetadata;

	private final Handler handler;

	public Reader(InputStream is, Handler handler, boolean fetchTags,
			boolean fetchMetadata)
	{
		this(new InputStreamCompactReader(is), handler, fetchTags,
				fetchMetadata);
	}

	public Reader(CompactReader reader, Handler handler, boolean fetchTags,
			boolean fetchMetadata)
	{
		super(reader);
		this.handler = handler;
		this.fetchTags = fetchTags;
		this.fetchMetadata = fetchMetadata;
	}

	public void run() throws IOException
	{
		FileHeader header = ReaderUtil.parseHeader(reader);
		hasMetadata = header.hasMetadata();
		handler.handle(header);

		long notifysize = 100 * 1024 * 1024;
		long processed = 0;
		long lastMessage = 0;

		while (true) {
			FileBlock block = readBlock();
			if (block == null) {
				break;
			}

			// TODO: not exactly correct here, because 2 is really a varInt,
			// but this is only for counting...
			int blockSize = 1 + 2 + block.getBuffer().length;
			processed += blockSize;
			long message = processed / notifysize;
			if (message > lastMessage) {
				lastMessage = message;
				System.err.println(
						String.format("%.3f MiB", processed / 1024.0 / 1024.0));
			}

			// System.out.println("type: " + block.getType());
			// System.out.println("#objects: " + block.getNumObjects());
			// System.out.println("#bytes: " + block.getBuffer().length);

			parseBlock(block);
		}

		handler.complete();
	}

	private void parseBlock(FileBlock block) throws IOException
	{
		byte[] uncompressed = Decompression.decompress(block);

		ByteArrayInputStream bais = new ByteArrayInputStream(uncompressed);
		CompactReader compactReader = new InputStreamCompactReader(bais);

		parseBlock(compactReader, block);
	}

	private void parseBlock(CompactReader reader, FileBlock block)
			throws IOException
	{

		// read objects
		if (block.getType() == Definitions.BLOCK_TYPE_NODES) {
			List<Node> nodes = ReaderUtil.parseNodes(reader, block, fetchTags,
					hasMetadata, fetchMetadata);
			for (Node node : nodes) {
				handler.handle(node);
			}
		} else if (block.getType() == Definitions.BLOCK_TYPE_WAYS) {
			List<Way> ways = ReaderUtil.parseWays(reader, block, fetchTags,
					hasMetadata, fetchMetadata);
			for (Way way : ways) {
				handler.handle(way);
			}
		} else if (block.getType() == Definitions.BLOCK_TYPE_RELATIONS) {
			List<Relation> relations = ReaderUtil.parseRelations(reader, block,
					fetchTags, hasMetadata, fetchMetadata);
			for (Relation relation : relations) {
				handler.handle(relation);
			}
		}
	}

}

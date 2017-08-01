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

import com.slimjars.dist.gnu.trove.iterator.TLongIterator;
import com.slimjars.dist.gnu.trove.list.TLongList;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.InputStreamCompactReader;
import de.topobyte.osm4j.core.access.OsmIdHandler;
import de.topobyte.osm4j.core.access.OsmIdReader;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.Decompression;

public class TboIdReader extends BlockReader implements OsmIdReader
{

	private OsmIdHandler handler;

	public TboIdReader(InputStream is)
	{
		this(new InputStreamCompactReader(is));
	}

	public TboIdReader(CompactReader reader)
	{
		super(reader);
	}

	@Override
	public void setIdHandler(OsmIdHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void read() throws OsmInputException
	{
		try {
			FileHeader header = ReaderUtil.parseHeader(reader);
			if (header.hasBounds()) {
				handler.handle(header.getBounds());
			}
		} catch (IOException e) {
			throw new OsmInputException("error while reading header", e);
		}

		while (true) {
			FileBlock block;
			try {
				block = readBlock();
			} catch (IOException e) {
				throw new OsmInputException("error while reading block", e);
			}
			if (block == null) {
				break;
			}

			try {
				parseBlock(block);
			} catch (IOException e) {
				throw new OsmInputException("error while parsing block", e);
			}
		}

		try {
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException("error while completing handler", e);
		}
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
			TLongList nodes = ReaderUtil.parseNodeIds(reader, block);
			TLongIterator iterator = nodes.iterator();
			while (iterator.hasNext()) {
				handler.handleNode(iterator.next());
			}
		} else if (block.getType() == Definitions.BLOCK_TYPE_WAYS) {
			TLongList ways = ReaderUtil.parseWayIds(reader, block);
			TLongIterator iterator = ways.iterator();
			while (iterator.hasNext()) {
				handler.handleWay(iterator.next());
			}
		} else if (block.getType() == Definitions.BLOCK_TYPE_RELATIONS) {
			TLongList relations = ReaderUtil.parseRelationIds(reader, block);
			TLongIterator iterator = relations.iterator();
			while (iterator.hasNext()) {
				handler.handleRelation(iterator.next());
			}
		}
	}

}

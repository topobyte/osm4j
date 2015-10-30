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

import gnu.trove.list.TLongList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.CompactReader;
import de.topobyte.osm4j.tbo.io.Decompression;
import de.topobyte.osm4j.tbo.io.InputStreamCompactReader;

public class TboIdIterator extends BlockReader implements OsmIdIterator
{

	private FileHeader header;

	private int available = 0;
	private int pointer = 0;

	private boolean valid = true;
	private FileBlock block = null;
	private EntityType entityType = EntityType.Node;
	private TLongList ids = null;

	public TboIdIterator(InputStream input) throws IOException
	{
		this(new InputStreamCompactReader(input));
	}

	public TboIdIterator(CompactReader reader) throws IOException
	{
		super(reader);

		header = ReaderUtil.parseHeader(reader);
	}

	@Override
	public boolean hasNext()
	{
		if (available == 0) {
			try {
				advanceBlock();
			} catch (IOException e) {
				return false;
			}
		}
		return valid && available > 0;
	}

	@Override
	public IdContainer next()
	{
		long id = ids.get(pointer);
		pointer += 1;
		available -= 1;
		return new IdContainer(entityType, id);
	}

	@Override
	public void remove()
	{
		// not implemented
	}

	private void advanceBlock() throws IOException
	{
		block = readBlock();
		if (block == null) {
			valid = false;
			return;
		}
		pointer = 0;
		available = block.getNumObjects();

		byte[] uncompressed = Decompression.decompress(block);

		ByteArrayInputStream bais = new ByteArrayInputStream(uncompressed);
		InputStreamCompactReader reader = new InputStreamCompactReader(bais);

		switch (block.getType()) {
		case Definitions.BLOCK_TYPE_NODES:
			entityType = EntityType.Node;
			ids = ReaderUtil.parseNodeIds(reader, block);
			break;
		case Definitions.BLOCK_TYPE_WAYS:
			entityType = EntityType.Way;
			ids = ReaderUtil.parseWayIds(reader, block);
			break;
		case Definitions.BLOCK_TYPE_RELATIONS:
			entityType = EntityType.Relation;
			ids = ReaderUtil.parseRelationIds(reader, block);
			break;
		}
	}

	@Override
	public Iterator<IdContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasBounds()
	{
		return header.hasBounds();
	}

	@Override
	public OsmBounds getBounds()
	{
		return header.getBounds();
	}

}

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
import java.util.Iterator;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.CompactReader;
import de.topobyte.osm4j.tbo.io.InputStreamCompactReader;

public class TboIterator extends BlockReader implements OsmIterator
{

	private FileHeader header;

	private int available = 0;
	private int pointer = 0;
	private int total = 0;

	private boolean valid = true;
	private FileBlock block = null;
	private EntityType entityType = EntityType.Node;
	private List<? extends OsmEntity> entities = null;

	public TboIterator(InputStream input) throws IOException
	{
		this(new InputStreamCompactReader(input));
	}

	public TboIterator(CompactReader reader) throws IOException
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
	public EntityContainer next()
	{
		OsmEntity entity = entities.get(pointer);
		pointer += 1;
		available -= 1;
		return new EntityContainer(entityType, entity);
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
		total = block.getNumObjects();
		available = total;

		byte[] buffer = block.getBuffer();
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		InputStreamCompactReader reader = new InputStreamCompactReader(bais);

		switch (block.getType()) {
		case Definitions.BLOCK_TYPE_NODES:
			entityType = EntityType.Node;
			entities = ReaderUtil.parseNodes(reader, block);
			break;
		case Definitions.BLOCK_TYPE_WAYS:
			entityType = EntityType.Way;
			entities = ReaderUtil.parseWays(reader, block);
			break;
		case Definitions.BLOCK_TYPE_RELATIONS:
			entityType = EntityType.Relation;
			entities = ReaderUtil.parseRelations(reader, block);
			break;
		}
	}

	@Override
	public Iterator<EntityContainer> iterator()
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

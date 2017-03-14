package de.topobyte.osm4j.extra.relations.split;

import de.topobyte.osm4j.extra.batch.SizeBatch;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;

class IdBboxEntryBatch extends SizeBatch<IdBboxEntry>
{

	IdBboxEntryBatch(int maxSize)
	{
		super(maxSize);
	}

	@Override
	protected int size(IdBboxEntry element)
	{
		return element.getSize();
	}

}

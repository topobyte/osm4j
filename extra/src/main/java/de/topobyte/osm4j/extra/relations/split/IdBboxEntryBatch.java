package de.topobyte.osm4j.extra.relations.split;

import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;

class IdBboxEntryBatch extends Batch<IdBboxEntry>
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

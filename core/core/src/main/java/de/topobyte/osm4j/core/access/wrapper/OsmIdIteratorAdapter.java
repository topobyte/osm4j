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

package de.topobyte.osm4j.core.access.wrapper;

import java.util.Iterator;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;

public class OsmIdIteratorAdapter implements OsmIdIterator
{

	private OsmIterator iterator;

	public OsmIdIteratorAdapter(OsmIterator iterator)
	{
		this.iterator = iterator;
	}

	@Override
	public Iterator<IdContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public IdContainer next()
	{
		EntityContainer container = iterator.next();
		return new IdContainer(container.getType(), container.getEntity()
				.getId());
	}

	@Override
	public void remove()
	{
		iterator.remove();
	}

	@Override
	public boolean hasBounds()
	{
		return iterator.hasBounds();
	}

	@Override
	public OsmBounds getBounds()
	{
		return iterator.getBounds();
	}

}

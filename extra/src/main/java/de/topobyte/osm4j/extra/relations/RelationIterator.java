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

package de.topobyte.osm4j.extra.relations;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;

public class RelationIterator implements Iterable<OsmRelation>,
		Iterator<OsmRelation>
{

	private OsmIterator iterator;

	public RelationIterator(OsmIterator iterator)
	{
		this.iterator = iterator;
	}

	@Override
	public Iterator<OsmRelation> iterator()
	{
		return this;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	private boolean valid = false;
	private boolean hasNext = false;
	private OsmRelation next = null;

	@Override
	public boolean hasNext()
	{
		if (valid) {
			return hasNext;
		}
		advance();
		return hasNext;
	}

	@Override
	public OsmRelation next()
	{
		if (valid) {
			return current();
		}
		advance();
		if (valid) {
			return current();
		}
		throw new NoSuchElementException();
	}

	private OsmRelation current()
	{
		OsmRelation current = next;
		valid = false;
		next = null;
		return current;
	}

	private void advance()
	{
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			valid = true;
			hasNext = true;
			next = (OsmRelation) container.getEntity();
			return;
		}
		valid = true;
		hasNext = false;
		next = null;
	}

}

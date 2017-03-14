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

package de.topobyte.osm4j.utils.buffer;

import de.topobyte.osm4j.core.model.iface.EntityContainer;

public class EntityBuffer
{

	private int position = 0;
	private int size = 0;

	private EntityContainer[] elements;

	public EntityBuffer(int n)
	{
		elements = new EntityContainer[n];
	}

	public void add(EntityContainer e)
	{
		elements[size++] = e;
	}

	public boolean isEmpty()
	{
		return position == size;
	}

	public int size()
	{
		return size - position;
	}

	public EntityContainer remove()
	{
		EntityContainer container = elements[position];
		elements[position++] = null;
		return container;
	}

	public void clear()
	{
		position = 0;
		size = 0;
	}

}

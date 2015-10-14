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

package de.topobyte.osm4j.geometry.relation;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import gnu.trove.stack.TLongStack;
import gnu.trove.stack.array.TLongArrayStack;

class LongSetStack
{

	private TLongSet set = new TLongHashSet();
	private TLongStack stack = new TLongArrayStack();

	/**
	 * It is not allowed to push values that are already contained in the set.
	 */
	public void push(long value)
	{
		set.add(value);
		stack.push(value);
	}

	public boolean contains(long value)
	{
		return set.contains(value);
	}

	public long pop()
	{
		long value = stack.pop();
		set.remove(value);
		return value;
	}

	public long peek()
	{
		return stack.peek();
	}

	public int size()
	{
		return stack.size();
	}

	public boolean isEmpty()
	{
		return set.isEmpty();
	}

}

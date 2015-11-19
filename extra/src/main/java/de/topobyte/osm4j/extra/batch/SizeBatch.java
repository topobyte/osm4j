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

package de.topobyte.osm4j.extra.batch;


public abstract class SizeBatch<T> extends AbstractBatch<T>
{

	private int maxSize;

	private int size = 0;

	public SizeBatch(int maxSize)
	{
		this.maxSize = maxSize;
	}

	protected abstract int size(T element);

	@Override
	public void clear()
	{
		super.clear();
		size = 0;
	}

	@Override
	public boolean fits(T element)
	{
		if (elements.isEmpty()) {
			return true;
		}
		if (size + size(element) <= maxSize) {
			return true;
		}
		return false;
	}

	@Override
	public void add(T element)
	{
		super.add(element);
		size += size(element);
	}

	public int getSize()
	{
		return size;
	}

	@Override
	public boolean isFull()
	{
		return size == maxSize;
	}

}

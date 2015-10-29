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

package de.topobyte.osm4j.tbo.batching;

public class ElementCountBatchBuilder<T> implements BatchBuilder<T>
{

	private int maxElements;
	private int counter = 0;

	public ElementCountBatchBuilder(int maxElements)
	{
		this.maxElements = maxElements;
	}

	@Override
	public void add(T element)
	{
		counter++;
	}

	@Override
	public boolean full()
	{
		return counter >= maxElements;
	}

	@Override
	public boolean fits(T element)
	{
		return counter < maxElements;
	}

	@Override
	public void clear()
	{
		counter = 0;
	}

	@Override
	public int bufferSizeHint()
	{
		return maxElements;
	}

}

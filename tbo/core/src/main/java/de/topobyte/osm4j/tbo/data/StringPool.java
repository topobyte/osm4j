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

package de.topobyte.osm4j.tbo.data;

import java.util.List;

import com.slimjars.dist.gnu.trove.map.TMap;

public class StringPool
{

	private final List<String> pool;
	private final TMap<String, StringPoolEntry> map;

	public StringPool(List<String> pool, TMap<String, StringPoolEntry> map)
	{
		this.pool = pool;
		this.map = map;
	}

	public int size()
	{
		return pool.size();
	}

	public String getString(int id)
	{
		String string = pool.get(id);
		return string;
	}

	public int getId(String string)
	{
		StringPoolEntry entry = map.get(string);
		return entry.value;
	}

}

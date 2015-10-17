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

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringPoolBuilder
{

	private TMap<String, StringPoolEntry> map = new THashMap<String, StringPoolEntry>();

	public void add(String string)
	{
		StringPoolEntry entry = map.get(string);
		if (entry == null) {
			map.put(string, new StringPoolEntry(string, 1));
		} else {
			entry.value += 1;
		}
	}

	public StringPool buildStringPool()
	{
		int size = map.size();
		final List<StringPoolEntry> list = new ArrayList<StringPoolEntry>(size);

		map.forEachValue(new TObjectProcedure<StringPoolEntry>() {

			@Override
			public boolean execute(StringPoolEntry entry)
			{
				list.add(entry);
				return true;
			}
		});
		Collections.sort(list);
		List<String> strings = new ArrayList<String>(size);
		int i = 0;
		for (StringPoolEntry entry : list) {
			strings.add(entry.key);
			entry.value = i++;
		}
		StringPool pool = new StringPool(strings, map);
		return pool;
	}

}

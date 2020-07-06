// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.nodedb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An index that stores information about the contained blocks of a database
 * within a sorted list.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Index implements Serializable
{

	private static final long serialVersionUID = 1372296450992507661L;

	private List<Entry> entries = new ArrayList<>();

	/**
	 * Add an entry that holds nodes with indices ranging from start to end
	 * whose block starts at position.
	 * 
	 * @param start
	 *            the first node's index.
	 * @param end
	 *            the last node's index.
	 * @param position
	 *            the position within the database file.
	 */
	public void addEntry(long start, long end, long position)
	{
		getEntries().add(new Entry(start, end, position));
	}

	/**
	 * Find an entry for the given node id.
	 * 
	 * @param id
	 *            the id to look for.
	 * @return the entry found or null if none is found.
	 */
	public Entry find(long id)
	{
		int i = Collections.binarySearch(getEntries(), new Entry(id, id, 0),
				new Comparator<Entry>() {

					@Override
					public int compare(Entry a, Entry b)
					{
						// System.out.println(a.start + ", " + a.end);
						// System.out.println(b.start + ", " + b.end);
						if (b.start < a.start) {
							return 1;
						} else if (b.end > a.end) {
							return -1;
						} else {
							return 0;
						}
					}
				});
		if (i >= 0) {
			return getEntries().get(i);
		}
		return null;
	}

	/**
	 * Getter for the underlying list of entires.
	 * 
	 * @return the list of entries.
	 */
	public List<Entry> getEntries()
	{
		return entries;
	}

}

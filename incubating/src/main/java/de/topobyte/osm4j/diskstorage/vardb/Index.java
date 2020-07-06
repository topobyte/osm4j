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

package de.topobyte.osm4j.diskstorage.vardb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An index stores information about the elements contained in blocks.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Index implements Serializable
{

	private static final long serialVersionUID = 4998934421071045174L;

	private List<Entry> entries = new ArrayList<>();

	/**
	 * @param e
	 *            add this entry to the list of maintained entries.
	 */
	public void addEntry(Entry e)
	{
		entries.add(e);
	}

	/**
	 * @return the list of entries available.
	 */
	public List<Entry> getEntries()
	{
		return entries;
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
		int i = Collections.binarySearch(getEntries(),
				new Entry(id, 0, id, 0, 0), new Comparator<Entry>() {

					@Override
					public int compare(Entry a, Entry b)
					{
						// System.out.println("a: " + a.start +"." +
						// a.startIndex + ", "
						// + a.end + "." + a.endIndex);
						// System.out.println("b: " + b.start +"." +
						// b.startIndex + ", "
						// + b.end + "." + b.endIndex);
						if (b.start < a.start) {
							return 1;
						} else if (b.end > a.end) {
							return -1;
						} else {
							if (a.start == b.start
									&& b.startIndex < a.startIndex) {
								return 1;
							} else if (a.end == b.end
									&& b.endIndex > a.endIndex) {
								return -1;
							} else {
								return 0;
							}
						}
					}
				});
		if (i >= 0) {
			return getEntries().get(i);
		}
		return null;
	}

}

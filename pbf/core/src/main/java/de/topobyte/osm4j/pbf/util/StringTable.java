/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package de.topobyte.osm4j.pbf.util;

import java.util.Arrays;
import java.util.Comparator;

import com.google.protobuf.ByteString;
import com.slimjars.dist.gnu.trove.map.TObjectIntMap;
import com.slimjars.dist.gnu.trove.map.hash.TObjectIntHashMap;

import de.topobyte.osm4j.pbf.protobuf.Osmformat;

/**
 * Class for mapping a set of strings to integers, giving frequently occurring
 * strings small integers.
 * 
 * @author Scott A. Crosby (scott@sacrosby.com)
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class StringTable
{

	private TObjectIntMap<String> counts = new TObjectIntHashMap<>(100);
	private TObjectIntMap<String> stringMap;
	private String set[];

	public void incr(String s)
	{
		if (counts.containsKey(s)) {
			counts.put(s, counts.get(s) + 1);
		} else {
			counts.put(s, 1);
		}
	}

	/**
	 * After the string table has been built, return the index of a string in
	 * it.
	 *
	 * Note, value '0' is reserved for use as a delimiter and will not be
	 * returned.
	 */
	public int getIndex(String s)
	{
		return stringMap.get(s);
	}

	public void finish()
	{
		Comparator<String> comparator = new Comparator<String>() {

			@Override
			public int compare(final String s1, String s2)
			{
				int diff = counts.get(s2) - counts.get(s1);
				return diff;
			}

		};

		/* Sort the string table */

		/*
		 * When a string is referenced, strings in the string table with
		 * indices: 0 : Is reserved (used as a delimiter in tags A: 1 to 127 :
		 * Uses can be represented with 1 byte B: 128 to 128**2-1 : Uses can be
		 * represented with 2 bytes, C: 128*128 to X : Uses can be represented
		 * with 3 bytes in the unlikely case we have >16k strings in a block. No
		 * block will contain enough strings that we'll need 4 bytes.
		 * 
		 * There are goals that will improve compression: 1. I want to use 1
		 * bytes for the most frequently occurring strings, then 2 bytes, then 3
		 * bytes. 2. I want to use low integers as frequently as possible (for
		 * better entropy encoding out of deflate) 3. I want the string table to
		 * compress as small as possible.
		 * 
		 * Condition 1 is obvious. Condition 2 makes deflate compress string
		 * table references more effectively. When compressing entities, delta
		 * coding causes small positive integers to occur more frequently than
		 * larger integers. Even though a string table references to indices of
		 * 1 and 127 both use one byte in a decompressed file, the small integer
		 * bias causes deflate to use fewer bits to represent the smaller index
		 * when compressed. Condition 3 is most effective when adjacent strings
		 * in the string table have a lot of common substrings.
		 * 
		 * So, when I decide on the master string table to use, I put the 127
		 * most frequently occurring strings into A (accomplishing goal 1), and
		 * sort them by frequency (to accomplish goal 2), but for B and C, which
		 * contain the less progressively less frequently encountered strings, I
		 * sort them lexicographically, to maximize goal 3 and ignoring goal 2.
		 * 
		 * Goal 1 is the most important. Goal 2 helped enough to be worth it,
		 * and goal 3 was pretty minor, but all should be re-benchmarked.
		 */

		set = counts.keySet().toArray(new String[0]);
		if (set.length > 0) {
			// Sort based on the frequency.
			Arrays.sort(set, comparator);
			// Each group of keys that serializes to the same number of bytes is
			// sorted lexicographically.
			// to maximize deflate compression.

			// Don't sort the first array. There's not likely to be much
			// benefit, and we want frequent values to be small.
			// Arrays.sort(set, Math.min(0, set.length-1), Math.min(1 << 7,
			// set.length-1));

			Arrays.sort(set, Math.min(1 << 7, set.length - 1),
					Math.min(1 << 14, set.length - 1));
			Arrays.sort(set, Math.min(1 << 14, set.length - 1),
					Math.min(1 << 21, set.length - 1), comparator);
		}
		stringMap = new TObjectIntHashMap<>(2 * set.length);
		for (int i = 0; i < set.length; i++) {
			// Index 0 is reserved for use as a delimiter.
			stringMap.put(set[i], Integer.valueOf(i + 1));
		}
		counts = null;
	}

	public void clear()
	{
		counts = new TObjectIntHashMap<>(100);
		stringMap = null;
		set = null;
	}

	public Osmformat.StringTable.Builder serialize()
	{
		Osmformat.StringTable.Builder builder = Osmformat.StringTable
				.newBuilder();
		// Add a unused string at offset 0 which is used as a delimiter.
		builder.addS(ByteString.copyFromUtf8(""));
		for (int i = 0; i < set.length; i++) {
			builder.addS(ByteString.copyFromUtf8(set[i]));
		}
		return builder;
	}

}

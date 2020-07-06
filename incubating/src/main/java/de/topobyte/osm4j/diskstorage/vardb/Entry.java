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

/**
 * An entry for maintaining block within the index.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Entry implements Serializable
{

	private static final long serialVersionUID = -4552693273952089083L;

	long start;
	int startIndex;
	long end;
	int endIndex;

	private long position;

	/**
	 * Create a new entry.
	 * 
	 * @param start
	 *            the first record parts' record's id.
	 * @param startIndex
	 *            the first record parts' part-number.
	 * @param end
	 *            the last record parts' record's id.
	 * @param endIndex
	 *            the last record parts' part-number.
	 * @param position
	 *            the position within a RandomAccessFile.
	 */
	public Entry(long start, int startIndex, long end, int endIndex,
			long position)
	{
		this.start = start;
		this.startIndex = startIndex;
		this.end = end;
		this.endIndex = endIndex;
		this.position = position;
	}

	/**
	 * @return the position within the RandomAccessFile.
	 */
	public long getPosition()
	{
		return position;
	}

}

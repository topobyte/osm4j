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

/**
 * An entry for the index.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Entry implements Serializable
{

	private static final long serialVersionUID = -8812170552597601088L;

	long start, end;

	private long position;

	/**
	 * Create a new entry. Represents a block of nodes.
	 * 
	 * @param start
	 *            the index of the first contained node.
	 * @param end
	 *            the index of the last contained node.
	 * @param position
	 *            the address of the block in the corresponding database file.
	 */
	public Entry(long start, long end, long position)
	{
		this.start = start;
		this.end = end;
		this.position = position;
	}

	/**
	 * Getter for the position.
	 * 
	 * @return the position in the database file.
	 */
	public long getPosition()
	{
		return position;
	}

}
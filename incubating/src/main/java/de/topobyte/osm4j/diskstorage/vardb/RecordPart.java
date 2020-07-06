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

/**
 * A RecordPart is a part of a record. It hold an array of bytes that is part of
 * a longer array of bytes that constitutes the data of the record.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class RecordPart
{

	private long id;
	private int index;
	private int total;
	private byte[] bytes;

	/**
	 * Create a new RecordPart.
	 * 
	 * @param id
	 *            the identifier of the record.
	 * @param index
	 *            the index within the list of parts that the record consists
	 *            of.
	 * @param total
	 *            the total number of record parts the record consists of.
	 * @param bytes
	 *            the number of bytes this part is holding.
	 */
	public RecordPart(long id, int index, int total, byte[] bytes)
	{
		this.id = id;
		this.index = index;
		this.total = total;
		this.bytes = bytes;
	}

	/**
	 * @return the identifier of the record.
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return the index within the array of parts the record consists of.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * @return the total number of parts this record consists of.
	 */
	public int getTotal()
	{
		return total;
	}

	/**
	 * @param total
	 *            the total number of parts this record consists of.
	 */
	public void setTotal(int total)
	{
		this.total = total;
	}

	/**
	 * @return the bytes this part provides.
	 */
	public byte[] getBytes()
	{
		return bytes;
	}

	/**
	 * @return the number of bytes this record provides.
	 */
	public int getLength()
	{
		return bytes.length;
	}

	@Override
	public String toString()
	{
		return getId() + "," + getIndex();
	}

}

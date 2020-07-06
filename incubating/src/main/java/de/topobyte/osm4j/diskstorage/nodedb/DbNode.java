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

import java.io.IOException;

import de.topobyte.osm4j.diskstorage.HighLevelInputStream;
import de.topobyte.osm4j.diskstorage.HighLevelOutputStream;

/**
 * The representation of a node for the node database.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class DbNode
{

	/**
	 * The number of bytes used to store a node.
	 */
	public static final int NODESIZE = 24;

	private long id;
	private double lon;
	private double lat;

	/**
	 * Create a node
	 * 
	 * @param id
	 *            the if
	 * @param lon
	 *            the longitude
	 * @param lat
	 *            the latitude
	 */
	public DbNode(long id, double lon, double lat)
	{
		this.id = id;
		this.lon = lon;
		this.lat = lat;
	}

	/**
	 * Write this node to an output stream
	 * 
	 * @param hlos
	 *            the output stream to write to
	 * @throws IOException
	 *             on writing failure.
	 */
	public void write(HighLevelOutputStream hlos) throws IOException
	{
		hlos.writeLong(getId());
		hlos.writeDouble(getLat());
		hlos.writeDouble(getLon());
	}

	/**
	 * Read this node from an input stream
	 * 
	 * @param hlis
	 *            the stream to read from
	 * @return the read node
	 * @throws IOException
	 *             on reading failure.
	 */
	public static DbNode read(HighLevelInputStream hlis) throws IOException
	{
		long id = hlis.readLong();
		double lat = hlis.readDouble();
		double lon = hlis.readDouble();
		return new DbNode(id, lon, lat);
	}

	/**
	 * @return this node's id.
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return this node's longitude.
	 */
	public double getLon()
	{
		return lon;
	}

	/**
	 * @return this node's latitude.
	 */
	public double getLat()
	{
		return lat;
	}

	@Override
	public String toString()
	{
		return id + ": " + lon + "," + lat;
	}

}
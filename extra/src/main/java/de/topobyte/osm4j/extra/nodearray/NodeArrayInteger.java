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

package de.topobyte.osm4j.extra.nodearray;

import java.io.File;
import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.extra.io.ra.BufferedRandomAccessFile;
import de.topobyte.osm4j.extra.io.ra.NormalRandomAccessFile;
import de.topobyte.osm4j.extra.io.ra.RandomAccess;

public class NodeArrayInteger implements NodeArray
{

	final static int NULL = 0;

	private RandomAccess f;

	public NodeArrayInteger(File file) throws IOException
	{
		f = new NormalRandomAccessFile(file);
	}

	public NodeArrayInteger(File file, int pageSize, int cacheSize)
			throws IOException
	{
		f = new BufferedRandomAccessFile(file, pageSize, cacheSize);
	}

	@Override
	public void close() throws IOException
	{
		f.close();
	}

	@Override
	public OsmNode get(long id) throws IOException
	{
		f.seek(id * 8);
		double lon = Coding.decodeLonFromInt(f.readInt());
		double lat = Coding.decodeLatFromInt(f.readInt());
		return new Node(id, lon, lat);
	}

	@Override
	public boolean supportsContainment()
	{
		return false;
	}

	@Override
	public boolean contains(long id) throws IOException
	{
		return true;
	}

}

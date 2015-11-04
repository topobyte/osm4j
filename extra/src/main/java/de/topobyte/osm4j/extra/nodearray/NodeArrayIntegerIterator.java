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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.extra.io.ra.BufferedRandomAccessFile;
import de.topobyte.osm4j.extra.io.ra.NormalRandomAccessFile;
import de.topobyte.osm4j.extra.io.ra.RandomAccess;
import de.topobyte.osm4j.extra.progress.NodeProgress;

public class NodeArrayIntegerIterator implements NodeArray
{

	public static void main(String[] args) throws IOException
	{
		NodeProgress p = new NodeProgress();
		p.printTimed(1000);

		InputStream fis = new FileInputStream(
				"/raid/osm/planet/planet-derivatives/150831/nodes.int.array");
		InputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);
		while (true) {
			int clon = dis.readInt();
			int clat = dis.readInt();
			if (clon == NULL && clat == NULL) {
				continue;
			}
			double lon = Coding.decodeLonFromInt(clon);
			double lat = Coding.decodeLatFromInt(clat);
			p.increment();
		}
	}

	final static int NULL = Coding.INT_NULL;

	private RandomAccess f;

	public NodeArrayIntegerIterator(RandomAccess f)
	{
		this.f = f;
	}

	public NodeArrayIntegerIterator(File file) throws IOException
	{
		f = new NormalRandomAccessFile(file);
	}

	public NodeArrayIntegerIterator(File file, int pageSize, int cacheSize)
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
		return true;
	}

	@Override
	public boolean contains(long id) throws IOException
	{
		f.seek(id * 8);
		double lon = Coding.decodeLonFromInt(f.readInt());
		double lat = Coding.decodeLatFromInt(f.readInt());
		return lon != NULL && lat != NULL;
	}

}

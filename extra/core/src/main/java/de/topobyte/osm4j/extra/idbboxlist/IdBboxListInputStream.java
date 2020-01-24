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

package de.topobyte.osm4j.extra.idbboxlist;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.locationtech.jts.geom.Envelope;

public class IdBboxListInputStream implements IdBboxInput
{

	private DataInputStream dataInput;

	public IdBboxListInputStream(InputStream input)
	{
		dataInput = new DataInputStream(input);
	}

	@Override
	public void close() throws IOException
	{
		dataInput.close();
	}

	@Override
	public IdBboxEntry next() throws IOException
	{
		long id = dataInput.readLong();
		double minX = dataInput.readDouble();
		double maxX = dataInput.readDouble();
		double minY = dataInput.readDouble();
		double maxY = dataInput.readDouble();
		int size = dataInput.readInt();
		return new IdBboxEntry(id, new Envelope(minX, maxX, minY, maxY), size);
	}

}

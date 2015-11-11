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

package de.topobyte.osm4j.extra.idlist;

import java.io.IOException;
import java.io.InputStream;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.InputStreamCompactReader;

public class IdListInputStream implements IdInput
{

	private InputStream input;
	private CompactReader reader;

	private long last = 0;

	public IdListInputStream(InputStream input)
	{
		this.input = input;
		reader = new InputStreamCompactReader(input);
	}

	@Override
	public void close() throws IOException
	{
		input.close();
	}

	@Override
	public long next() throws IOException
	{
		long diff = reader.readVariableLengthUnsignedInteger();
		long id = last + diff;
		last = id;
		return id;
	}

}

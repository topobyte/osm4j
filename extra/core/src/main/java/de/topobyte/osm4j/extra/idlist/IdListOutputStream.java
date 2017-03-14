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
import java.io.OutputStream;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;

public class IdListOutputStream
{

	private OutputStream output;
	private CompactWriter writer;

	private long last = 0;

	public IdListOutputStream(OutputStream output)
	{
		this.output = output;
		writer = new OutputStreamCompactWriter(output);
	}

	public void close() throws IOException
	{
		output.close();
	}

	public void write(long id) throws IOException
	{
		if (id <= last) {
			throw new IOException(String.format(
					"ids must be strictly monotonically increasing (%d <= %d)",
					id, last));
		}
		long diff = id - last;
		writer.writeVariableLengthUnsignedInteger(diff);
		last = id;
	}

}

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

package de.topobyte.osm4j.extra.datatree.closingstreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayReader
{

	private ByteArrayOutputStream output;
	private InputStream input;
	private boolean finished = false;

	public ByteArrayReader(ByteArrayOutputStream output, InputStream input)
	{
		this.output = output;
		this.input = input;
	}

	public boolean done()
	{
		return finished;
	}

	public void read() throws IOException
	{
		int b = input.read();
		if (b < 0) {
			finished = true;
		} else {
			output.write(b);
		}
	}

	public void read(int n) throws IOException
	{
		byte[] buffer = new byte[n];
		int len = input.read(buffer);
		if (len < 0) {
			finished = true;
		} else {
			output.write(buffer, 0, len);
		}
	}

}

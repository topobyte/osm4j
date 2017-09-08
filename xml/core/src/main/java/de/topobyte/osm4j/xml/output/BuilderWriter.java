// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.xml.output;

import java.io.IOException;
import java.io.Writer;

class BuilderWriter extends Writer
{

	private StringBuilder buf;

	public BuilderWriter()
	{
		buf = new StringBuilder();
	}

	public BuilderWriter(StringBuilder buf)
	{
		this.buf = buf;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		buf.append(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		// nothing to do
	}

	@Override
	public void close() throws IOException
	{
		// nothing to do
	}

	public void append(String string)
	{
		buf.append(string);
	}

	@Override
	public String toString()
	{
		return buf.toString();
	}

	public void append(int i)
	{
		buf.append(i);
	}

	public void append(long l)
	{
		buf.append(l);
	}

}

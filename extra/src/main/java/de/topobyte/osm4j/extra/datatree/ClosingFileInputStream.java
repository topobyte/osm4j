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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ClosingFileInputStream extends InputStream
{

	private ClosingFileInputStreamFactory factory;
	private File file;
	private int id;

	public ClosingFileInputStream(ClosingFileInputStreamFactory factory,
			File file, int id)
	{
		this.factory = factory;
		this.file = file;
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	@Override
	public void close() throws IOException
	{
		factory.close(id);
	}

	@Override
	public int read() throws IOException
	{
		InputStream fis = factory.create(file, id);
		return fis.read();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		InputStream fis = factory.create(file, id);
		return fis.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		InputStream fis = factory.create(file, id);
		return fis.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException
	{
		InputStream fis = factory.create(file, id);
		return fis.skip(n);
	}

	@Override
	public int available() throws IOException
	{
		InputStream fis = factory.create(file, id);
		return fis.available();
	}

}

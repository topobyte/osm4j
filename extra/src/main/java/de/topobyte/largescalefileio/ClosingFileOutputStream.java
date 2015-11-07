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

package de.topobyte.largescalefileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ClosingFileOutputStream extends OutputStream
{

	private ClosingFileOutputStreamFactory factory;
	private File file;
	private int id;
	private boolean append = false;

	public ClosingFileOutputStream(ClosingFileOutputStreamFactory factory,
			File file, int id) throws IOException
	{
		this.factory = factory;
		this.file = file;
		this.id = id;

		// Open and close the file to emulate FileOutputStream behavior
		// concerning file truncation
		FileOutputStream out = new FileOutputStream(file);
		out.close();
	}

	public int getId()
	{
		return id;
	}

	@Override
	public void write(int b) throws IOException
	{
		OutputStream fos = factory.create(file, id, append);
		append = true;
		fos.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		OutputStream fos = factory.create(file, id, append);
		append = true;
		fos.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		OutputStream fos = factory.create(file, id, append);
		append = true;
		fos.write(b, off, len);
	}

	@Override
	public void flush() throws IOException
	{
		factory.flush(id);
	}

	@Override
	public void close() throws IOException
	{
		factory.close(id);
	}

}

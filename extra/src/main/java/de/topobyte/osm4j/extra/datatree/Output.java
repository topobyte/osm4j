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
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmOutputStream;

public class Output
{

	private File file;
	private OutputStream output;
	private OsmOutputStream osmOutput;
	private int count = 0;

	Output(File file, OutputStream output, OsmOutputStream osmOutput)
	{
		this.file = file;
		this.output = output;
		this.osmOutput = osmOutput;
	}

	public File getFile()
	{
		return file;
	}

	public OutputStream getOutputStream()
	{
		return output;
	}

	public OsmOutputStream getOsmOutput()
	{
		return osmOutput;
	}

	public int getCount()
	{
		return count;
	}

	public void incrementCounter()
	{
		count++;
	}

}

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

package de.topobyte.osm4j.extra.relations;

import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmOutputStream;

class Output
{

	private Path file;
	private OutputStream output;
	private OsmOutputStream osmOutput;
	private long count = 0;

	Output(Path file, OutputStream output, OsmOutputStream osmOutput)
	{
		this.file = file;
		this.output = output;
		this.osmOutput = osmOutput;
	}

	public Path getFile()
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

	public long getCount()
	{
		return count;
	}

	public void incrementCounter()
	{
		count++;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

}

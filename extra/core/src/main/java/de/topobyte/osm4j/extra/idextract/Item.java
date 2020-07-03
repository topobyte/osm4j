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

package de.topobyte.osm4j.extra.idextract;

import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.extra.entitywriter.EntityWriter;
import de.topobyte.osm4j.extra.idlist.IdInput;

class Item
{

	private long next;
	private IdInput input;
	private OutputStream output;
	private OsmOutputStream osmOutput;
	private EntityWriter writer;

	public Item(IdInput input, OutputStream output, OsmOutputStream osmOutput,
			EntityWriter writer) throws IOException
	{
		this.input = input;
		this.output = output;
		this.osmOutput = osmOutput;
		this.writer = writer;
		next = input.next();
	}

	public long getNext()
	{
		return next;
	}

	public void next() throws IOException
	{
		next = input.next();
	}

	public void close() throws IOException
	{
		input.close();
	}

	public OutputStream getOutput()
	{
		return output;
	}

	public OsmOutputStream getOsmOutput()
	{
		return osmOutput;
	}

	public EntityWriter getWriter()
	{
		return writer;
	}

}

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

package de.topobyte.osm4j.extra.datatree.nodetree;

import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.extra.datatree.Node;

class NodeOutput extends OsmOutputStreamStreamOutput
{

	private Node node;
	private Path file;
	private long count = 0;

	NodeOutput(Node node, Path file, OutputStream output,
			OsmOutputStream osmOutput)
	{
		super(output, osmOutput);
		this.node = node;
		this.file = file;
	}

	public Node getNode()
	{
		return node;
	}

	public Path getFile()
	{
		return file;
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

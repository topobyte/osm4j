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

package de.topobyte.osm4j.extra.datatree.ways;

import java.io.IOException;

import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idlist.IdListInputStream;

class IdInput
{

	private Node node;
	private long next;
	private IdListInputStream input;

	public IdInput(Node node, IdListInputStream input) throws IOException
	{
		this.node = node;
		this.input = input;
		next = input.next();
	}

	public Node getNode()
	{
		return node;
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

}

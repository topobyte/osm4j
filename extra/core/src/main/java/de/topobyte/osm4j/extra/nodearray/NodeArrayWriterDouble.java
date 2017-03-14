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

package de.topobyte.osm4j.extra.nodearray;

import java.io.DataOutputStream;
import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.OsmNode;

public class NodeArrayWriterDouble extends BaseNodeArrayWriter
{

	public NodeArrayWriterDouble(DataOutputStream out)
	{
		super(out);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		while (lastId < node.getId() - 1) {
			out.writeLong(NodeArrayDouble.NULL);
			out.writeLong(NodeArrayDouble.NULL);
			lastId++;
		}
		out.writeDouble(node.getLongitude());
		out.writeDouble(node.getLatitude());
		lastId++;
	}

}

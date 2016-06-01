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
import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;

public class NodeArrayCreator
{

	private OsmIterator input;
	private Path outputPath;
	private NodeArrayType type;
	private NodeArrayWriter writer;

	public NodeArrayCreator(OsmIterator input, Path outputPath,
			NodeArrayType type)
	{
		this.input = input;
		this.outputPath = outputPath;
		this.type = type;
	}

	public void execute() throws IOException
	{
		initOutput();
		run();
	}

	private void initOutput() throws IOException
	{
		OutputStream bos = StreamUtil.bufferedOutputStream(outputPath);
		DataOutputStream out = new DataOutputStream(bos);

		switch (type) {
		default:
		case DOUBLE:
			writer = new NodeArrayWriterDouble(out);
			break;
		case FLOAT:
			writer = new NodeArrayWriterFloat(out);
			break;
		case INTEGER:
			writer = new NodeArrayWriterInteger(out);
			break;
		case SHORT:
			writer = new NodeArrayWriterShort(out);
			break;
		}
	}

	private void run() throws IOException
	{
		while (input.hasNext()) {
			EntityContainer container = input.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			writer.write(node);
		}
		writer.finish();
	}

}

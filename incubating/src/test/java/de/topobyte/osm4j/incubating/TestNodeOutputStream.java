// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.incubating;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.utils.StreamUtil;

public class TestNodeOutputStream
{

	public static void main(String[] args) throws IOException
	{
		NodeProgress progress = new NodeProgress();
		progress.printTimed(1000);

		String pathInput = "/tmp/berlin/nodes.tbo";
		String pathOutput = "/tmp/berlin/nodes.list";

		InputStream input = StreamUtil.bufferedInputStream(pathInput);
		OutputStream output = StreamUtil.bufferedOutputStream(pathOutput);

		TboIterator iterator = new TboIterator(input, true, true);

		// RunnableBufferBridge bridge = new RunnableBufferBridge(iterator,
		// buffer);
		// Thread thread = new Thread(bridge);
		// thread.start();

		// TboWriter writer = new TboWriter(output, true, true);

		NodeOutputStream writer = new NodeOutputStream(output, true, true);

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			switch (container.getType()) {
			case Node:
				OsmNode node = (OsmNode) container.getEntity();
				writer.write(node);
				break;
			case Way:
				break;
			case Relation:
				break;
			}
			progress.increment();
		}
		progress.stop();

		writer.close();
	}

}

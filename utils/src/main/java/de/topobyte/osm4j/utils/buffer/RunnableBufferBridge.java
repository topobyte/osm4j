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

package de.topobyte.osm4j.utils.buffer;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class RunnableBufferBridge implements StoppableRunnable
{

	private final OsmIterator input;
	private final OsmBuffer output;

	private boolean stopped = false;

	public RunnableBufferBridge(OsmIterator input, OsmBuffer output)
	{
		this.input = input;
		this.output = output;
	}

	@Override
	public void stop()
	{
		stopped = true;
		output.setInvalid();
	}

	@Override
	public void run()
	{
		try {
			if (!stopped && input.hasBounds()) {
				output.write(input.getBounds());
			}
			while (!stopped && input.hasNext()) {
				EntityContainer container = input.next();
				switch (container.getType()) {
				case Node:
					output.write((OsmNode) container.getEntity());
					break;
				case Way:
					output.write((OsmWay) container.getEntity());
					break;
				case Relation:
					output.write((OsmRelation) container.getEntity());
					break;
				}
			}
			if (!stopped) {
				output.complete();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

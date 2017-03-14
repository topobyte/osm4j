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

package de.topobyte.osm4j.extra.ways;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.threading.Buffer;
import de.topobyte.osm4j.utils.buffer.StoppableRunnable;

public class RunnableWayBatchBuilder implements StoppableRunnable
{

	private int maxWays;
	private int maxWayNodes;

	private final OsmIterator input;
	private Buffer<WayBatch> output;

	private boolean stopped = false;

	public RunnableWayBatchBuilder(OsmIterator input, int maxWays,
			int maxWayNodes, Buffer<WayBatch> output)
	{
		this.input = input;
		this.maxWays = maxWays;
		this.maxWayNodes = maxWayNodes;
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
			WayBatch batch = new WayBatch(maxWays, maxWayNodes);
			while (!stopped && input.hasNext()) {
				EntityContainer container = input.next();
				switch (container.getType()) {
				case Way:
					OsmWay way = (OsmWay) container.getEntity();
					if (way.getNumberOfNodes() == 0) {
						continue;
					}
					if (batch.fits(way)) {
						batch.add(way);
					} else {
						System.out.println("does not fit");
						output.write(batch);
						System.out.println("write returned");
						batch = new WayBatch(maxWays, maxWayNodes);
						batch.add(way);
					}
					break;
				case Node:
					break;
				case Relation:
					break;
				}
			}
			if (!stopped) {
				if (!batch.getElements().isEmpty()) {
					output.write(batch);
				}
				output.complete();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

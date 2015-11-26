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

package de.topobyte.osm4j.extra.datatree.nodetree.distribute;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.progress.NodeProgress;

public abstract class NodeIteratorRunnable implements Runnable
{

	private OsmIterator iterator;

	public NodeIteratorRunnable(OsmIterator iterator)
	{
		this.iterator = iterator;
	}

	@Override
	public void run()
	{
		NodeProgress counter = new NodeProgress();
		counter.printTimed(1000);
		try {
			loop: while (iterator.hasNext()) {
				EntityContainer entityContainer = iterator.next();
				switch (entityContainer.getType()) {
				case Node:
					handle((OsmNode) entityContainer.getEntity());
					counter.increment();
					break;
				case Way:
					break loop;
				case Relation:
					break loop;
				}
			}
			finished();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			counter.stop();
		}
	}

	protected abstract void handle(OsmNode node) throws IOException;

	protected abstract void finished() throws IOException;

}

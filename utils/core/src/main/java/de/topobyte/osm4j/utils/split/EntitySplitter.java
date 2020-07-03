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

package de.topobyte.osm4j.utils.split;

import java.io.IOException;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class EntitySplitter extends AbstractEntitySplitter
{

	public EntitySplitter(OsmIterator iterator, Path pathNodes, Path pathWays,
			Path pathRelations, OsmOutputConfig outputConfig)
	{
		super(iterator, pathNodes, pathWays, pathRelations, outputConfig);
	}

	public void execute() throws IOException
	{
		init();
		passBounds();
		run();
		finish();
	}

	private void run() throws IOException
	{
		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				if (passNodes) {
					oosNodes.write((OsmNode) entityContainer.getEntity());
				}
				break;
			case Way:
				if (passWays) {
					oosWays.write((OsmWay) entityContainer.getEntity());
				} else if (!passRelations) {
					break loop;
				}
				break;
			case Relation:
				if (passRelations) {
					oosRelations.write((OsmRelation) entityContainer
							.getEntity());
				} else {
					break loop;
				}
				break;
			}
		}
	}

}

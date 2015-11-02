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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIteratorSingleOutput;

public class OsmDropMetadata extends
		AbstractTaskSingleInputIteratorSingleOutput
{

	@Override
	protected String getHelpMessage()
	{
		return OsmDropMetadata.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmDropMetadata convert = new OsmDropMetadata();

		convert.setup(args);

		convert.readMetadata = false;
		convert.writeMetadata = false;

		convert.init();

		convert.run();

		convert.finish();
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
	}

	protected void run() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
			switch (entityContainer.getType()) {
			case Node: {
				OsmNode node = (OsmNode) entityContainer.getEntity();
				osmOutputStream.write(node);
				break;
			}
			case Way: {
				OsmWay way = (OsmWay) entityContainer.getEntity();
				osmOutputStream.write(way);
				break;
			}
			case Relation: {
				OsmRelation relation = (OsmRelation) entityContainer
						.getEntity();
				osmOutputStream.write(relation);
				break;
			}
			}
		}
		osmOutputStream.complete();
	}

}

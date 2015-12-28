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

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;

public class OsmCalculateBbox extends AbstractExecutableSingleInputStream
{

	@Override
	protected String getHelpMessage()
	{
		return OsmCalculateBbox.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmCalculateBbox task = new OsmCalculateBbox();
		task.setup(args);

		task.readMetadata = false;
		task.readTags = false;
		task.init();

		task.run();

		task.finish();
	}

	private Envelope envelope = new Envelope();

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				OsmNode node = (OsmNode) entityContainer.getEntity();
				envelope.expandToInclude(node.getLongitude(),
						node.getLatitude());
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}

		BBox bbox = new BBox(envelope);
		System.out.println(BBoxString.create(bbox));

		finish();
	}

}

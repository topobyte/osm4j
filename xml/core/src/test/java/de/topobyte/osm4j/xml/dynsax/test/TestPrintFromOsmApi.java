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

package de.topobyte.osm4j.xml.dynsax.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class TestPrintFromOsmApi
{

	public static void main(String[] args) throws IOException
	{
		String url = "https://api.openstreetmap.org/api/0.6/node/240109189";
		InputStream input = new URL(url).openStream();

		OsmIterator iterator = new OsmXmlIterator(input, true);

		OsmXmlOutputStream writer = new OsmXmlOutputStream(System.out, true);

		for (EntityContainer container : iterator) {
			if (container.getType() == EntityType.Node) {
				writer.write((OsmNode) container.getEntity());
			}
		}

		writer.complete();
	}

}

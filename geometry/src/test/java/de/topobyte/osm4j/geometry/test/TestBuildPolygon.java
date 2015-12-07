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

package de.topobyte.osm4j.geometry.test;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.io.WKTWriter;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class TestBuildPolygon
{

	public static void main(String[] args) throws MalformedURLException,
			IOException, ParserConfigurationException, SAXException,
			EntityNotFoundException
	{
		String query = "http://overpass-api.de/api/interpreter?data=(rel(8638);>;);out;";

		// Open a stream
		InputStream input = new URL(query).openStream();

		OsmIterator iterator = new OsmXmlIterator(input, false);
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		TLongObjectMap<OsmRelation> relations = data.getRelations();

		if (relations.isEmpty()) {
			System.out.println("No relation found");
			return;
		}

		TLongObjectIterator<OsmRelation> ri = relations.iterator();
		ri.advance();
		OsmRelation relation = ri.value();

		File outputDir = new File("/tmp");
		String filename = String.format("relation-%d.wkt", relation.getId());
		File file = new File(outputDir, filename);

		GeometryBuilder geometryBuilder = new GeometryBuilder();
		MultiPolygon polygon = geometryBuilder.build(relation, data);
		WKTWriter writer = new WKTWriter();
		FileWriter fileWriter = new FileWriter(file);
		writer.write(polygon, fileWriter);
		fileWriter.close();

		System.out.println("Successfully created WKT representation");
	}

}

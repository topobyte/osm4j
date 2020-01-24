// Copyright 2019 Sebastian Kuerten
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.utils.AbstractExecutable;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class OsmDownloadRegionGeometry extends AbstractExecutable
{

	@Override
	protected String getHelpMessage()
	{
		return OsmDownloadRegionGeometry.class.getSimpleName()
				+ " [options] <relation id> <output file>";
	}

	public static void main(String[] args)
			throws IOException, EntityNotFoundException
	{
		OsmDownloadRegionGeometry task = new OsmDownloadRegionGeometry();

		task.setup(args);

		task.run();
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
	}

	private void run() throws IOException, EntityNotFoundException
	{
		List<String> args = line.getArgList();
		if (args.size() != 2) {
			System.out.println("Please specify exaclty two arguments");
			new HelpFormatter().printHelp(getHelpMessage(), options);
			System.exit(1);
		}

		String sId = args.get(0);
		long id = Long.parseLong(sId);

		Path pathOutput = Paths.get(args.get(1));

		String query = String.format(
				"http://www.overpass-api.de/api/interpreter?data=relation(%d);>>;out;",
				id);

		InputStream input = new URL(query).openStream();
		OsmIterator iterator = new OsmXmlIterator(input, true);
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		OsmRelation relation = data.getRelation(id);
		GeometryBuilder builder = new GeometryBuilder();
		Geometry geometry = builder.build(relation, data);

		Geometry buffer = geometry.buffer(0);

		BufferedWriter writer = Files.newBufferedWriter(pathOutput);
		new WKTWriter().write(buffer, writer);
		writer.close();
	}

}

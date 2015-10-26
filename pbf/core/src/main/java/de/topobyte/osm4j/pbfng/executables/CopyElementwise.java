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

package de.topobyte.osm4j.pbfng.executables;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbfng.seq.PbfParser;
import de.topobyte.osm4j.pbfng.seq.PbfWriter;

public class CopyElementwise
{

	public static void main(String[] args) throws IOException,
			OsmInputException
	{
		if (args.length != 2) {
			System.out.println("usage: "
					+ CopyElementwise.class.getSimpleName()
					+ " <input> <output>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		OutputStream output = new FileOutputStream(args[1]);

		final PbfWriter writer = new PbfWriter(output, true);

		PbfParser parser = new PbfParser(new OsmHandler() {

			@Override
			public void handle(OsmBounds bounds) throws IOException
			{
				writer.write(bounds);
			}

			@Override
			public void handle(OsmNode node) throws IOException
			{
				writer.write(node);
			}

			@Override
			public void handle(OsmWay way) throws IOException
			{
				writer.write(way);
			}

			@Override
			public void handle(OsmRelation relation) throws IOException
			{
				writer.write(relation);
			}

			@Override
			public void complete() throws IOException
			{
				writer.complete();
			}

		}, true);

		parser.parse(input);

		output.close();
	}
}

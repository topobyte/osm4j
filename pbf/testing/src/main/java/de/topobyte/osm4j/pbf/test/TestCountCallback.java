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

package de.topobyte.osm4j.pbf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfParser;

public class TestCountCallback implements OsmHandler
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: "
					+ TestCountCallback.class.getSimpleName() + " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		TestCountCallback test = new TestCountCallback();

		PbfParser parser = new PbfParser(test, false);

		FileInputStream input = new FileInputStream(file);
		parser.parse(input);
	}

	private int nc = 0, wc = 0, rc = 0;

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		// ignore bounds
	}

	@Override
	public void handle(OsmNode node)
	{
		nc++;
	}

	@Override
	public void handle(OsmWay way)
	{
		wc++;
	}

	@Override
	public void handle(OsmRelation relation)
	{
		rc++;
	}

	@Override
	public void complete()
	{
		System.out.println("nodes: " + nc);
		System.out.println("ways: " + wc);
		System.out.println("relations: " + rc);
	}

}

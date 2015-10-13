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
import java.util.Iterator;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.pbf.access.PbfIterator;

public class TestCountIterator
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: "
					+ TestCountIterator.class.getSimpleName() + " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		FileInputStream input = new FileInputStream(file);

		Iterator<EntityContainer> iterator = new PbfIterator(input, false);

		long nc = 0, wc = 0, rc = 0;

		while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				nc++;
				break;
			case Way:
				wc++;
				break;
			case Relation:
				rc++;
				break;
			}
		}

		System.out.println("nodes: " + nc);
		System.out.println("ways: " + wc);
		System.out.println("relations: " + rc);
	}

}

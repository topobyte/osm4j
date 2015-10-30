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

package de.topobyte.osm4j.tbo.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import de.topobyte.compactio.CompactReader;
import de.topobyte.compactio.InputStreamCompactReader;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.tbo.access.TboIterator;

public class TestCountIterator
{

	private static long nc = 0, wc = 0, rc = 0;

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: "
					+ TestCountIterator.class.getSimpleName() + " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		FileInputStream input = new FileInputStream(file);
		CompactReader compactReader = new InputStreamCompactReader(input);

		TboIterator iterator = new TboIterator(compactReader);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			switch (container.getType()) {
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

		System.out.println("nodes:     " + nc);
		System.out.println("ways:      " + wc);
		System.out.println("relations: " + rc);
	}

}

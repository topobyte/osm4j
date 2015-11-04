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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;

public class DataTreeUtil
{

	public static void writeTreeInfo(File dir, BBox bbox)
			throws FileNotFoundException
	{
		File file = new File(dir, DataTree.FILENAME_INFO);

		PrintWriter pw = new PrintWriter(file);
		pw.println(DataTree.PROPERTY_BBOX + ": " + BBoxString.create(bbox));
		pw.close();
	}

}

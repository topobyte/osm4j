// Copyright 2016 Sebastian Kuerten
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
import java.io.FileWriter;
import java.io.IOException;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.io.WKTWriter;

public class DataTreeBoxGeometryCreator
{

	private File dirTree;
	private File fileOutput;

	public DataTreeBoxGeometryCreator(File dirTree, File fileOutput)
	{
		this.dirTree = dirTree;
		this.fileOutput = fileOutput;
	}

	public void execute() throws IOException
	{
		System.out.println("Opening data tree: " + dirTree);

		DataTree tree = DataTreeOpener.open(dirTree);
		GeometryCollection geometry = BoxUtil.createBoxesGeometry(tree,
				BoxUtil.WORLD_BOUNDS);

		System.out.println("Writing output to: " + fileOutput);

		FileWriter writer = new FileWriter(fileOutput);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

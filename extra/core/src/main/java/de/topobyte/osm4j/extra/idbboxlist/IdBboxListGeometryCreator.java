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

package de.topobyte.osm4j.extra.idbboxlist;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;

public class IdBboxListGeometryCreator
{

	private File fileInput;
	private File fileOutput;

	public IdBboxListGeometryCreator(File fileInput, File fileOutput)
	{
		this.fileInput = fileInput;
		this.fileOutput = fileOutput;
	}

	public void execute() throws IOException
	{
		System.out.println("Opening file: " + fileInput);

		List<Geometry> boxList = IdBboxUtil.readBoxes(fileInput);

		GeometryCollection geometry = new GeometryFactory()
				.createGeometryCollection(boxList.toArray(new Geometry[0]));

		System.out.println("Writing output to: " + fileOutput);

		FileWriter writer = new FileWriter(fileOutput);
		new WKTWriter().write(geometry, writer);
		writer.close();
	}

}

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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdBboxListGeometryCreator
{

	final static Logger logger = LoggerFactory
			.getLogger(IdBboxListGeometryCreator.class);

	private Path fileInput;
	private Path fileOutput;

	public IdBboxListGeometryCreator(Path fileInput, Path fileOutput)
	{
		this.fileInput = fileInput;
		this.fileOutput = fileOutput;
	}

	public void execute() throws IOException
	{
		logger.info("Opening file: " + fileInput);

		List<Geometry> boxList = IdBboxUtil.readBoxes(fileInput);

		GeometryCollection geometry = new GeometryFactory()
				.createGeometryCollection(boxList.toArray(new Geometry[0]));

		logger.info("Writing output to: " + fileOutput);

		try (BufferedWriter writer = Files.newBufferedWriter(fileOutput)) {
			new WKTWriter().write(geometry, writer);
		}
	}

}

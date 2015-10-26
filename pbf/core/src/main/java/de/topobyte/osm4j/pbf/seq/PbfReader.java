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

package de.topobyte.osm4j.pbf.seq;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;

public class PbfReader implements OsmReader
{

	private OsmHandler handler;

	private boolean parseMetadata;
	private InputStream input;

	public PbfReader(InputStream input, boolean parseMetadata)
	{
		this.input = input;
		this.parseMetadata = parseMetadata;
	}

	public PbfReader(File file, boolean parseMetadata)
			throws FileNotFoundException
	{
		InputStream fis = new FileInputStream(file);
		input = new BufferedInputStream(fis);
		this.parseMetadata = parseMetadata;
	}

	public PbfReader(String pathname, boolean parseMetadata)
			throws FileNotFoundException
	{
		this(new File(pathname), parseMetadata);
	}

	@Override
	public void setHandler(OsmHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void read() throws OsmInputException
	{
		PbfParser parser = new PbfParser(handler, parseMetadata);
		try {
			parser.parse(input);
		} catch (IOException e) {
			throw new OsmInputException("error while parsing data", e);
		}

		try {
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException("error while completing handler", e);
		}
	}

}

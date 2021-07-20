// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.changeset.dynsax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.topobyte.osm4j.core.access.OsmInputException;

/**
 * This is a SAX-based parser for OSM changesets data.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmChangesetsReader
{

	private OsmChangesetsHandler handler;

	private InputStream inputStream;

	public OsmChangesetsReader(InputStream inputStream)
	{
		this.inputStream = inputStream;
	}

	public OsmChangesetsReader(File file) throws FileNotFoundException
	{
		InputStream fis = new FileInputStream(file);
		inputStream = new BufferedInputStream(fis);
	}

	public OsmChangesetsReader(String pathname) throws FileNotFoundException
	{
		this(new File(pathname));
	}

	public void setHandler(OsmChangesetsHandler handler)
	{
		this.handler = handler;
	}

	public void read() throws OsmInputException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			parser = saxParserFactory.newSAXParser();
		} catch (Exception e) {
			throw new OsmInputException("error while creating xml parser", e);
		}

		OsmChangesetsSaxHandler saxHandler = OsmChangesetsSaxHandler
				.createInstance(handler);

		try {
			parser.parse(inputStream, saxHandler);
		} catch (Exception e) {
			throw new OsmInputException("error while parsing changesets data",
					e);
		}

		try {
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException("error while completing handler", e);
		}
	}

}

// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.osc.dynsax;

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
 * This is a SAX-based parser for OSM OSC data.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmOscReader
{

	private OsmChangeHandler handler;

	private boolean parseMetadata;
	private InputStream inputStream;

	public OsmOscReader(InputStream inputStream, boolean parseMetadata)
	{
		this.inputStream = inputStream;
		this.parseMetadata = parseMetadata;
	}

	public OsmOscReader(File file, boolean parseMetadata)
			throws FileNotFoundException
	{
		InputStream fis = new FileInputStream(file);
		inputStream = new BufferedInputStream(fis);
		this.parseMetadata = parseMetadata;
	}

	public OsmOscReader(String pathname, boolean parseMetadata)
			throws FileNotFoundException
	{
		this(new File(pathname), parseMetadata);
	}

	public void setHandler(OsmChangeHandler handler)
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

		OsmSaxHandler saxHandler = OsmSaxHandler.createInstance(handler,
				parseMetadata);

		try {
			parser.parse(inputStream, saxHandler);
		} catch (Exception e) {
			throw new OsmInputException("error while parsing OSC data", e);
		}

		try {
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException("error while completing handler", e);
		}
	}

}

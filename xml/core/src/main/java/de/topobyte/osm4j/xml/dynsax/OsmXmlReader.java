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

package de.topobyte.osm4j.xml.dynsax;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmXmlReader implements OsmReader, OsmHandler
{

	private OsmHandler handler;

	private boolean parseMetadata;
	private InputStream inputStream;

	public OsmXmlReader(InputStream inputStream, boolean parseMetadata)
	{
		this.inputStream = inputStream;
		this.parseMetadata = parseMetadata;
	}

	@Override
	public void setHandler(OsmHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void read() throws OsmInputException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser parser;
		try {
			parser = saxParserFactory.newSAXParser();
		} catch (Exception e) {
			throw new OsmInputException("error while creating xml parser", e);
		}

		OsmSaxHandler saxHandler = OsmSaxHandler.createInstance(this,
				parseMetadata);

		try {
			parser.parse(inputStream, saxHandler);
		} catch (Exception e) {
			throw new OsmInputException("error while parsing xml data", e);
		}

		try {
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException("error while completing handler", e);
		}
	}

	@Override
	public void handle(OsmNode node) throws IOException
	{
		handler.handle(node);
	}

	@Override
	public void handle(OsmWay way) throws IOException
	{
		handler.handle(way);
	}

	@Override
	public void handle(OsmRelation relation) throws IOException
	{
		handler.handle(relation);
	}

	@Override
	public void complete() throws IOException
	{
		handler.complete();
	}

}

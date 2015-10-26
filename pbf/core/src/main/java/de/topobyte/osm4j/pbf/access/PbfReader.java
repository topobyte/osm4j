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

package de.topobyte.osm4j.pbf.access;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import crosby.binary.file.FileBlock;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.PbfParser;

public class PbfReader implements OsmHandler, OsmReader
{

	private InputStream input;
	private PbfParser parser;
	private OsmHandler handler;

	public PbfReader(InputStream input, boolean fetchMetadata)
	{
		this.input = input;
		this.parser = new PbfParser(this, fetchMetadata);
	}

	@Override
	public void setHandler(OsmHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void read() throws OsmInputException
	{
		while (true) {
			try {
				FileBlock.process(input, parser);
			} catch (EOFException eof) {
				try {
					parser.complete();
					break;
				} catch (IOException e) {
					throw new OsmInputException(
							"error while completing parsing", e);
				}
			} catch (IOException e) {
				throw new OsmInputException("error while processing block", e);
			}
		}
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		handler.handle(bounds);
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

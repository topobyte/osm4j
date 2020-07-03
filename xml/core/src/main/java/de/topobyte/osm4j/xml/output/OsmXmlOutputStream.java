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

package de.topobyte.osm4j.xml.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmXmlOutputStream implements OsmOutputStream
{

	private final String newline = "\n";
	private final PrintWriter out;
	private final XmlWriter writer;

	public OsmXmlOutputStream(PrintWriter out, boolean printMetadata)
	{
		this.out = out;
		this.writer = new XmlWriter("  ", "    ", newline, printMetadata);
		writeHeader();
	}

	public OsmXmlOutputStream(OutputStream os, boolean printMetadata)
	{
		this(new PrintWriter(os), printMetadata);
	}

	private void writeHeader()
	{
		out.println("<?xml version='1.0' encoding='UTF-8'?>");
		out.println("<osm version=\"0.6\">");
	}

	@Override
	public void complete()
	{
		out.println("</osm>");
		out.flush();
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, bounds);
		buf.append(newline);
		out.print(buf.toString());
	}

	@Override
	public void write(OsmNode node)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, node);
		buf.append(newline);
		out.print(buf.toString());
	}

	@Override
	public void write(OsmWay way)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, way);
		buf.append(newline);
		out.print(buf.toString());
	}

	@Override
	public void write(OsmRelation relation)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, relation);
		buf.append(newline);
		out.print(buf.toString());
	}

}

// Copyright 2017 Sebastian Kuerten
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

import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmXmlSerializer
{

	private XmlWriter writer;

	public OsmXmlSerializer(boolean printMetadata)
	{
		writer = new XmlWriter("", "  ", "\n", printMetadata);
	}

	public OsmXmlSerializer(String indent1, String indent2,
			boolean printMetadata)
	{
		writer = new XmlWriter(indent1, indent2, "\n", printMetadata);
	}

	public OsmXmlSerializer(String indent1, String indent2, String newline,
			boolean printMetadata)
	{
		writer = new XmlWriter(indent1, indent2, newline, printMetadata);
	}

	public String write(OsmBounds bounds)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, bounds);
		return buf.toString();
	}

	public void write(StringBuilder buf, OsmBounds bounds)
	{
		BuilderWriter builder = new BuilderWriter(buf);
		writer.write(builder, bounds);
	}

	public String write(OsmNode node)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, node);
		return buf.toString();
	}

	public void write(StringBuilder buf, OsmNode node)
	{
		BuilderWriter builder = new BuilderWriter(buf);
		writer.write(builder, node);
	}

	public String write(OsmWay way)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, way);
		return buf.toString();
	}

	public void write(StringBuilder buf, OsmWay way)
	{
		BuilderWriter builder = new BuilderWriter(buf);
		writer.write(builder, way);
	}

	public String write(OsmRelation relation)
	{
		BuilderWriter buf = new BuilderWriter();
		writer.write(buf, relation);
		return buf.toString();
	}

	public void write(StringBuilder buf, OsmRelation relation)
	{
		BuilderWriter builder = new BuilderWriter(buf);
		writer.write(builder, relation);
	}

}

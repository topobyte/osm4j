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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmXmlOutputStream implements OsmOutputStream
{

	private final PrintWriter out;
	private final boolean printMetadata;

	public OsmXmlOutputStream(PrintWriter out, boolean printMetadata)
	{
		this.out = out;
		this.printMetadata = printMetadata;
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

	private DecimalFormat f = new DecimalFormat("0.#######;-0.#######",
			new DecimalFormatSymbols(Locale.US));

	private DateTimeFormatter formatter = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private CharSequenceTranslator escaper = StringEscapeUtils.ESCAPE_XML11;
	private String newline = "\n";

	private String templateBounds = "  <bounds minlon=\"%f\" minlat=\"%f\" maxlon=\"%f\" maxlat=\"%f\"/>";

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		out.println(String.format(templateBounds, bounds.getLeft(),
				bounds.getBottom(), bounds.getRight(), bounds.getTop()));
	}

	@Override
	public void write(OsmNode node)
	{
		BuilderWriter buf = new BuilderWriter();
		buf.append("  <node id=\"");
		buf.append(node.getId());
		buf.append("\"");
		buf.append(" lat=\"");
		buf.append(f.format(node.getLatitude()));
		buf.append("\"");
		buf.append(" lon=\"");
		buf.append(f.format(node.getLongitude()));
		buf.append("\"");
		if (printMetadata) {
			OsmMetadata metadata = node.getMetadata();
			printMetadata(buf, metadata);
		}
		if (node.getNumberOfTags() == 0) {
			buf.append("/>");
			buf.append(newline);
		} else {
			buf.append(">");
			buf.append(newline);
			printTags(buf, node);
			buf.append("  </node>");
			buf.append(newline);
		}
		out.print(buf.toString());
	}

	@Override
	public void write(OsmWay way)
	{
		BuilderWriter buf = new BuilderWriter();
		buf.append("  <way id=\"");
		buf.append(way.getId());
		buf.append("\"");
		if (printMetadata) {
			OsmMetadata metadata = way.getMetadata();
			printMetadata(buf, metadata);
		}
		if (way.getNumberOfTags() == 0 && way.getNumberOfNodes() == 0) {
			buf.append("/>");
			buf.append(newline);
		} else {
			buf.append(">");
			buf.append(newline);
			for (int i = 0; i < way.getNumberOfNodes(); i++) {
				long nodeId = way.getNodeId(i);
				buf.append("    <nd ref=\"");
				buf.append(nodeId);
				buf.append("\"/>");
				buf.append(newline);
			}
			printTags(buf, way);
			buf.append("  </way>");
			buf.append(newline);
		}
		out.print(buf.toString());
	}

	@Override
	public void write(OsmRelation relation)
	{
		BuilderWriter buf = new BuilderWriter();
		buf.append("  <relation id=\"");
		buf.append(relation.getId());
		buf.append("\"");
		if (printMetadata) {
			OsmMetadata metadata = relation.getMetadata();
			printMetadata(buf, metadata);
		}
		if (relation.getNumberOfTags() == 0
				&& relation.getNumberOfMembers() == 0) {
			buf.append("/>");
			buf.append(newline);
		} else {
			buf.append(">");
			buf.append(newline);
			for (int i = 0; i < relation.getNumberOfMembers(); i++) {
				OsmRelationMember member = relation.getMember(i);
				EntityType type = member.getType();
				String t = type == EntityType.Node ? "node"
						: type == EntityType.Way ? "way" : "relation";
				String role = member.getRole();
				role = escaper.translate(role);
				buf.append("    <member type=\"");
				buf.append(t);
				buf.append("\" ref=\"");
				buf.append(member.getId());
				buf.append("\" role=\"");
				buf.append(role);
				buf.append("\"/>");
				buf.append(newline);
			}
			printTags(buf, relation);
			buf.append("  </relation>");
			buf.append(newline);
		}
		out.print(buf.toString());
	}

	private void printMetadata(BuilderWriter buf, OsmMetadata metadata)
	{
		if (metadata == null) {
			return;
		}
		buf.append(" version=\"");
		buf.append(metadata.getVersion());
		buf.append("\"");
		buf.append(" timestamp=\"");
		buf.append(formatter.print(metadata.getTimestamp()));
		buf.append("\"");
		if (metadata.getUid() >= 0) {
			buf.append(" uid=\"");
			buf.append(metadata.getUid());
			buf.append("\"");
			String user = metadata.getUser();
			if (user != null) {
				user = escaper.translate(user);
			}
			buf.append(" user=\"");
			buf.append(user);
			buf.append("\"");
		}
		buf.append(" changeset=\"");
		buf.append(metadata.getChangeset());
		buf.append("\"");
		if (!metadata.isVisible()) {
			buf.append(" visible=\"false\"");
		}
	}

	private void printTags(BuilderWriter buf, OsmEntity entity)
	{
		for (int i = 0; i < entity.getNumberOfTags(); i++) {
			OsmTag tag = entity.getTag(i);
			String key = tag.getKey();
			String value = tag.getValue();
			buf.append("    <tag k=\"");
			buf.append(escaper.translate(key));
			buf.append("\" v=\"");
			buf.append(escaper.translate(value));
			buf.append("\"/>");
			buf.append(newline);
		}
	}
}

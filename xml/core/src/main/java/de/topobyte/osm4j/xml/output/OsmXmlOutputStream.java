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
		buf.append("  <node id=\"" + node.getId() + "\"");
		buf.append(" lat=\"" + f.format(node.getLatitude()) + "\"");
		buf.append(" lon=\"" + f.format(node.getLongitude()) + "\"");
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
		buf.append("  <way id=\"" + way.getId() + "\"");
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
				buf.append("    <nd ref=\"" + nodeId + "\"/>");
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
		buf.append("  <relation id=\"" + relation.getId() + "\"");
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
				buf.append("    <member type=\"" + t + "\" ref=\""
						+ member.getId() + "\" role=\"" + role + "\"/>");
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
		buf.append(" version=\"" + metadata.getVersion() + "\"");
		buf.append(" timestamp=\"" + formatter.print(metadata.getTimestamp())
				+ "\"");
		if (metadata.getUid() >= 0) {
			buf.append(" uid=\"" + metadata.getUid() + "\"");
			String user = metadata.getUser();
			if (user != null) {
				user = escaper.translate(user);
			}
			buf.append(" user=\"" + user + "\"");
		}
		buf.append(" changeset=\"" + metadata.getChangeset() + "\"");
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
			key = escaper.translate(key);
			value = escaper.translate(value);
			buf.append("    <tag k=\"" + key + "\" v=\"" + value + "\"/>");
			buf.append(newline);
		}
	}

}

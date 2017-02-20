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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
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

	// private CharSequenceTranslator escaper = new LookupTranslator(
	// EntityArrays.BASIC_ESCAPE());

	private CharSequenceTranslator escaper = StringEscapeUtils.ESCAPE_XML;

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
		out.print("  <node id=\"" + node.getId() + "\"");
		out.print(" lat=\"" + f.format(node.getLatitude()) + "\"");
		out.print(" lon=\"" + f.format(node.getLongitude()) + "\"");
		if (printMetadata) {
			OsmMetadata metadata = node.getMetadata();
			printMetadata(metadata);
		}
		if (node.getNumberOfTags() == 0) {
			out.println("/>");
		} else {
			out.println(">");
			printTags(node);
			out.println("  </node>");
		}
	}

	@Override
	public void write(OsmWay way)
	{
		out.print("  <way id=\"" + way.getId() + "\"");
		if (printMetadata) {
			OsmMetadata metadata = way.getMetadata();
			printMetadata(metadata);
		}
		if (way.getNumberOfTags() == 0 && way.getNumberOfNodes() == 0) {
			out.println("/>");
		} else {
			out.println(">");
			for (int i = 0; i < way.getNumberOfNodes(); i++) {
				long nodeId = way.getNodeId(i);
				out.println("    <nd ref=\"" + nodeId + "\"/>");
			}
			printTags(way);
			out.println("  </way>");
		}
	}

	@Override
	public void write(OsmRelation relation)
	{
		out.print("  <relation id=\"" + relation.getId() + "\"");
		if (printMetadata) {
			OsmMetadata metadata = relation.getMetadata();
			printMetadata(metadata);
		}
		if (relation.getNumberOfTags() == 0
				&& relation.getNumberOfMembers() == 0) {
			out.println("/>");
		} else {
			out.println(">");
			for (int i = 0; i < relation.getNumberOfMembers(); i++) {
				OsmRelationMember member = relation.getMember(i);
				EntityType type = member.getType();
				String t = type == EntityType.Node ? "node"
						: type == EntityType.Way ? "way" : "relation";
				String role = member.getRole();
				role = StringEscapeUtils.escapeXml(role);
				out.println("    <member type=\"" + t + "\" ref=\""
						+ member.getId() + "\" role=\"" + role + "\"/>");
			}
			printTags(relation);
			out.println("  </relation>");
		}
	}

	private void printMetadata(OsmMetadata metadata)
	{
		if (metadata == null) {
			return;
		}
		out.print(" version=\"" + metadata.getVersion() + "\"");
		out.print(" timestamp=\"" + formatter.print(metadata.getTimestamp())
				+ "\"");
		if (metadata.getUid() >= 0) {
			out.print(" uid=\"" + metadata.getUid() + "\"");
			String user = metadata.getUser();
			if (user != null) {
				user = escaper.translate(user);
			}
			out.print(" user=\"" + user + "\"");
		}
		out.print(" changeset=\"" + metadata.getChangeset() + "\"");
		if (!metadata.isVisible()) {
			out.print(" visible=\"false\"");
		}
	}

	private void printTags(OsmEntity entity)
	{
		for (int i = 0; i < entity.getNumberOfTags(); i++) {
			OsmTag tag = entity.getTag(i);
			String key = tag.getKey();
			String value = tag.getValue();
			key = escaper.translate(key);
			value = escaper.translate(value);
			out.println("    <tag k=\"" + key + "\" v=\"" + value + "\"/>");
		}
	}

}

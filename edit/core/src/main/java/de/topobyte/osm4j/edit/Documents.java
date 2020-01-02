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

package de.topobyte.osm4j.edit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.slimjars.dist.gnu.trove.list.TLongList;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.xml4jah.core.DocumentWriterConfig;
import de.topobyte.xml4jah.dom.DocumentWriter;

public class Documents
{

	private static Document document() throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		return docBuilder.newDocument();
	}

	public static Document createChangeset() throws ParserConfigurationException
	{
		Document document = document();

		Element eOsm = document.createElement("osm");
		document.appendChild(eOsm);
		Element eChangeset = document.createElement("changeset");
		eOsm.appendChild(eChangeset);
		Element eTag = document.createElement("tag");
		eChangeset.appendChild(eTag);
		eTag.setAttribute("k", "created_by");
		eTag.setAttribute("v", "osm4j-edit 0.0.1");

		return document;
	}

	public static Document createNode(Changeset changeset, double lon,
			double lat, List<? extends OsmTag> tags)
			throws ParserConfigurationException
	{
		Document document = document();

		Element eOsm = document.createElement("osm");
		document.appendChild(eOsm);
		Element eNode = document.createElement("node");
		eOsm.appendChild(eNode);
		eNode.setAttribute("changeset", Long.toString(changeset.getId()));
		eNode.setAttribute("lon", Double.toString(lon));
		eNode.setAttribute("lat", Double.toString(lat));

		if (tags != null) {
			addTags(document, eNode, tags);
		}

		return document;
	}

	public static Document createNode(Changeset changeset, OsmNode node)
			throws ParserConfigurationException
	{
		List<? extends OsmTag> tags = OsmModelUtil.getTagsAsList(node);
		return createNode(changeset, node.getLongitude(), node.getLatitude(),
				tags);
	}

	public static Document createWay(Changeset changeset, OsmWay way)
			throws ParserConfigurationException
	{
		Document document = document();

		Element eOsm = document.createElement("osm");
		document.appendChild(eOsm);
		Element eWay = document.createElement("way");
		eOsm.appendChild(eWay);
		eWay.setAttribute("changeset", Long.toString(changeset.getId()));

		addTags(document, eWay, way);

		TLongList nodes = OsmModelUtil.nodesAsList(way);
		for (long id : nodes.toArray()) {
			Element eNd = document.createElement("nd");
			eWay.appendChild(eNd);
			eNd.setAttribute("ref", Long.toString(id));
		}

		return document;
	}

	public static Document createRelation(Changeset changeset,
			OsmRelation relation) throws ParserConfigurationException
	{
		Document document = document();

		Element eOsm = document.createElement("osm");
		document.appendChild(eOsm);
		Element eRelation = document.createElement("relation");
		eOsm.appendChild(eRelation);
		eRelation.setAttribute("changeset", Long.toString(changeset.getId()));

		addTags(document, eRelation, relation);

		List<OsmRelationMember> members = OsmModelUtil.membersAsList(relation);
		for (OsmRelationMember member : members) {
			Element eMember = document.createElement("member");
			eRelation.appendChild(eMember);
			eMember.setAttribute("type", typename(member.getType()));
			eMember.setAttribute("ref", Long.toString(member.getId()));
			if (member.getRole() != null) {
				eMember.setAttribute("role", member.getRole());
			}
		}

		return document;
	}

	private static String typename(EntityType type)
	{
		switch (type) {
		case Node:
			return "node";
		case Way:
			return "way";
		case Relation:
			return "relation";
		default:
			return null;
		}
	}

	private static void addTags(Document document, Element eEntity,
			OsmEntity entity)
	{
		List<? extends OsmTag> tags = OsmModelUtil.getTagsAsList(entity);
		addTags(document, eEntity, tags);
	}

	private static void addTags(Document document, Element eEntity,
			List<? extends OsmTag> tags)
	{
		for (OsmTag tag : tags) {
			Element eTag = document.createElement("tag");
			eEntity.appendChild(eTag);
			eTag.setAttribute("k", tag.getKey());
			eTag.setAttribute("v", tag.getValue());
		}
	}

	public static Document deleteNode(Changeset changeset, long id, int version,
			double lon, double lat) throws ParserConfigurationException
	{
		Document document = document();
		Element eNode = deleteEntity(document, changeset, "node", id, version);
		eNode.setAttribute("lon", Double.toString(lon));
		eNode.setAttribute("lat", Double.toString(lat));
		return document;
	}

	public static Document deleteWay(Changeset changeset, long id, int version)
			throws ParserConfigurationException
	{
		Document document = document();
		deleteEntity(document, changeset, "way", id, version);
		return document;
	}

	public static Document deleteRelation(Changeset changeset, long id,
			int version) throws ParserConfigurationException
	{
		Document document = document();
		deleteEntity(document, changeset, "relation", id, version);
		return document;
	}

	private static Element deleteEntity(Document document, Changeset changeset,
			String type, long id, int version)
	{
		Element eOsm = document.createElement("osm");
		document.appendChild(eOsm);
		Element eEntity = document.createElement(type);
		eOsm.appendChild(eEntity);
		eEntity.setAttribute("id", Long.toString(id));
		eEntity.setAttribute("version", Integer.toString(version));
		eEntity.setAttribute("changeset", Long.toString(changeset.getId()));
		return eEntity;
	}

	public static String toString(Document document) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DocumentWriterConfig config = new DocumentWriterConfig();
		config.setWithDeclaration(false);
		DocumentWriter docWriter = new DocumentWriter(config);
		docWriter.write(document, baos);
		return new String(baos.toByteArray());
	}

}

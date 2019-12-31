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

import de.topobyte.osm4j.core.model.iface.OsmTag;
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
		eTag.setAttribute("v", "test 1.0");

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
			for (OsmTag tag : tags) {
				Element eTag = document.createElement("tag");
				eNode.appendChild(eTag);
				eTag.setAttribute("k", tag.getKey());
				eTag.setAttribute("v", tag.getValue());
			}
		}

		return document;
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

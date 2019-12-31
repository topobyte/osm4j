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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.topobyte.xml4jah.core.DocumentWriterConfig;
import de.topobyte.xml4jah.dom.DocumentWriter;

public class Documents
{

	public static Document createChangeset() throws ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		Document document = docBuilder.newDocument();

		Element osm = document.createElement("osm");
		document.appendChild(osm);
		Element changeset = document.createElement("changeset");
		osm.appendChild(changeset);
		Element tag = document.createElement("tag");
		changeset.appendChild(tag);
		tag.setAttribute("k", "created_by");
		tag.setAttribute("v", "test 1.0");

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

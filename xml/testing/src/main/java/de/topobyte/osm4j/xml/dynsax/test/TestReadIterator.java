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

package de.topobyte.osm4j.xml.dynsax.test;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class TestReadIterator
{

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException
	{
		if (args.length != 1) {
			System.out.println("usage: "
					+ TestReadIterator.class.getSimpleName()
					+ " <input osm xml>");
			System.exit(1);
		}

		String pathInput = args[0];

		TestReadIterator test = new TestReadIterator();

		OsmXmlIterator iterator = new OsmXmlIterator(pathInput, false);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				test.handle((OsmNode) container.getEntity());
			} else if (container.getType() == EntityType.Way) {
				test.handle((OsmWay) container.getEntity());
			} else if (container.getType() == EntityType.Relation) {
				test.handle((OsmRelation) container.getEntity());
			}
		}
	}

	public void handle(OsmNode node)
	{
		System.out.println("node " + node.getId() + ", lon: "
				+ node.getLongitude() + ", lat: " + node.getLatitude());
		printTags(node);

	}

	public void handle(OsmWay way)
	{
		System.out.println("way " + way.getId());
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long ref = way.getNodeId(i);
			System.out.println("member: " + ref);
		}
		printTags(way);
	}

	public void handle(OsmRelation relation)
	{
		System.out.println("relation " + relation.getId());
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			System.out.println("member " + member.getId() + ", type: "
					+ member.getType() + ", role: " + member.getRole());
		}
		printTags(relation);
	}

	private void printTags(OsmEntity entity)
	{
		if (entity.getNumberOfTags() == 0) {
			return;
		}
		System.out.println(OsmModelUtil.getTagsAsMap(entity));
	}

}

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

package de.topobyte.osm4j.osc.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.osc.dynsax.OsmOscReader;

public class TestReadCallback implements OsmHandler
{

	public static void main(String[] args) throws ParserConfigurationException,
			OsmInputException, FileNotFoundException
	{
		if (args.length != 1) {
			System.out
					.println("usage: " + TestReadCallback.class.getSimpleName()
							+ " <input osm osc>");
			System.exit(1);
		}

		String pathInput = args[0];

		TestReadCallback test = new TestReadCallback();
		OsmReader reader = new OsmOscReader(pathInput, false);

		reader.setHandler(test);
		reader.read();
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		System.out.println("bounds: " + bounds);
	}

	@Override
	public void handle(OsmNode node)
	{
		System.out.println("node " + node.getId() + ", lon: "
				+ node.getLongitude() + ", lat: " + node.getLatitude());
		printTags(node);

	}

	@Override
	public void handle(OsmWay way)
	{
		System.out.println("way " + way.getId());
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long ref = way.getNodeId(i);
			System.out.println("member: " + ref);
		}
		printTags(way);
	}

	@Override
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

	@Override
	public void complete() throws IOException
	{
		System.out.println("complete");
	}

}

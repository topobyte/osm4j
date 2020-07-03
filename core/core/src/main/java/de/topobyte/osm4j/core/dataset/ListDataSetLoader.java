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

package de.topobyte.osm4j.core.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.access.OsmReaderInput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;

public class ListDataSetLoader
{

	public static InMemoryListDataSet read(OsmIteratorInput iteratorInput,
			boolean keepNodeTags, boolean keepWayTags, boolean keepRelationTags)
			throws IOException
	{
		OsmIterator iterator = iteratorInput.getIterator();
		InMemoryListDataSet data = read(iterator, keepNodeTags, keepWayTags,
				keepRelationTags);
		iteratorInput.close();
		return data;
	}

	public static InMemoryListDataSet read(OsmReaderInput readerInput,
			boolean keepNodeTags, boolean keepWayTags, boolean keepRelationTags)
			throws IOException, OsmInputException
	{
		OsmReader reader = readerInput.getReader();
		InMemoryListDataSet data = read(reader, keepNodeTags, keepWayTags,
				keepRelationTags);
		readerInput.close();
		return data;
	}

	public static InMemoryListDataSet read(OsmIterator iterator,
			boolean keepNodeTags, boolean keepWayTags, boolean keepRelationTags)
			throws IOException
	{
		InMemoryListDataSet dataSet = new InMemoryListDataSet();

		List<OsmNode> nodes = dataSet.getNodes();
		List<OsmWay> ways = dataSet.getWays();
		List<OsmRelation> relations = dataSet.getRelations();

		if (iterator.hasBounds()) {
			dataSet.setBounds(iterator.getBounds());
		}

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			switch (container.getType()) {
			case Node:
				OsmNode node = (OsmNode) container.getEntity();
				if (!keepNodeTags) {
					node = new Node(node.getId(), node.getLongitude(),
							node.getLatitude());
				}
				nodes.add(node);
				break;
			case Way:
				OsmWay way = (OsmWay) container.getEntity();
				if (!keepWayTags) {
					TLongArrayList ids = new TLongArrayList();
					for (int i = 0; i < way.getNumberOfNodes(); i++) {
						ids.add(way.getNodeId(i));
					}
					way = new Way(way.getId(), ids);
				}
				ways.add(way);
				break;
			case Relation:
				OsmRelation relation = (OsmRelation) container.getEntity();
				if (!keepRelationTags) {
					List<OsmRelationMember> members = new ArrayList<>();
					for (int i = 0; i < relation.getNumberOfMembers(); i++) {
						members.add(relation.getMember(i));
					}
					relation = new Relation(relation.getId(), members);
				}
				relations.add(relation);
				break;
			}
		}

		return dataSet;
	}

	public static InMemoryListDataSet read(OsmReader reader,
			final boolean keepNodeTags, final boolean keepWayTags,
			final boolean keepRelationTags) throws OsmInputException
	{
		final InMemoryListDataSet dataSet = new InMemoryListDataSet();

		final List<OsmNode> nodes = dataSet.getNodes();
		final List<OsmWay> ways = dataSet.getWays();
		final List<OsmRelation> relations = dataSet.getRelations();

		reader.setHandler(new OsmHandler() {

			@Override
			public void handle(OsmBounds bounds) throws IOException
			{
				dataSet.setBounds(bounds);
			}

			@Override
			public void handle(OsmNode node) throws IOException
			{
				if (!keepNodeTags) {
					node = new Node(node.getId(), node.getLongitude(), node
							.getLatitude());
				}
				nodes.add(node);
			}

			@Override
			public void handle(OsmWay way) throws IOException
			{
				if (!keepWayTags) {
					TLongArrayList ids = new TLongArrayList();
					for (int i = 0; i < way.getNumberOfNodes(); i++) {
						ids.add(way.getNodeId(i));
					}
					way = new Way(way.getId(), ids);
				}
				ways.add(way);
			}

			@Override
			public void handle(OsmRelation relation) throws IOException
			{
				if (!keepRelationTags) {
					List<OsmRelationMember> members = new ArrayList<>();
					for (int i = 0; i < relation.getNumberOfMembers(); i++) {
						members.add(relation.getMember(i));
					}
					relation = new Relation(relation.getId(), members);
				}
				relations.add(relation);
			}

			@Override
			public void complete() throws IOException
			{
				// nothing to do here
			}

		});

		reader.read();

		return dataSet;
	}

}

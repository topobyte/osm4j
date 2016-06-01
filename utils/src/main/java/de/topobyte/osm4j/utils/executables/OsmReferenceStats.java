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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmReferenceStats extends AbstractExecutableSingleInputStream
{

	private static final String OPTION_NO_WAY_NODES = "no_way_nodes";
	private static final String OPTION_NO_RELATION_NODES = "no_relation_nodes";
	private static final String OPTION_NO_RELATION_WAYS = "no_relation_ways";
	private static final String OPTION_NO_RELATION_RELATIONS = "no_relation_relations";

	@Override
	protected String getHelpMessage()
	{
		return OsmReferenceStats.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmReferenceStats task = new OsmReferenceStats();
		task.setup(args);

		task.readMetadata = false;
		task.init();

		task.run();

		task.finish();
	}

	public OsmReferenceStats()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_NO_WAY_NODES, false, false, "do not check for way nodes");
		OptionHelper.addL(options, OPTION_NO_RELATION_NODES, false, false, "do not check for relation node members");
		OptionHelper.addL(options, OPTION_NO_RELATION_WAYS, false, false, "do not check for relation way members");
		OptionHelper.addL(options, OPTION_NO_RELATION_RELATIONS, false, false, "do not check for relation relation members");
		// @formatter:on
	}

	private boolean checkWayNodes;
	private boolean checkRelationNodes;
	private boolean checkRelationWays;
	private boolean checkRelationRelations;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		checkWayNodes = !line.hasOption(OPTION_NO_WAY_NODES);
		checkRelationNodes = !line.hasOption(OPTION_NO_RELATION_NODES);
		checkRelationWays = !line.hasOption(OPTION_NO_RELATION_WAYS);
		checkRelationRelations = !line.hasOption(OPTION_NO_RELATION_RELATIONS);
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				false);

		System.out.println("Nodes: " + data.getNodes().size());
		System.out.println("Ways: " + data.getWays().size());
		System.out.println("Relations: " + data.getRelations().size());

		int incompleteWays = 0;
		int incompleteRelations = 0;

		if (checkWayNodes) {
			for (OsmWay way : data.getWays().valueCollection()) {
				if (!isComplete(way, data)) {
					incompleteWays++;
				}
			}
		}

		if (checkRelationNodes || checkRelationWays || checkRelationRelations) {
			for (OsmRelation relation : data.getRelations().valueCollection()) {
				if (!isComplete(relation, data)) {
					incompleteRelations++;
				}
			}
		}

		System.out.println("Incomplete ways: " + incompleteWays);
		System.out.println("Incomplete relations: " + incompleteRelations);

		finish();
	}

	private boolean isComplete(OsmWay way, InMemoryMapDataSet data)
	{
		try {
			for (int i = 0; i < way.getNumberOfNodes(); i++) {
				data.getNode(way.getNodeId(i));
			}
		} catch (EntityNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean isComplete(OsmRelation relation, InMemoryMapDataSet data)
	{
		try {
			for (int i = 0; i < relation.getNumberOfMembers(); i++) {
				OsmRelationMember member = relation.getMember(i);
				if (member.getType() == EntityType.Node && checkRelationNodes) {
					data.getNode(member.getId());
				} else if (member.getType() == EntityType.Way
						&& checkRelationWays) {
					data.getWay(member.getId());
				} else if (member.getType() == EntityType.Relation
						&& checkRelationRelations) {
					data.getRelation(member.getId());
				}
			}
		} catch (EntityNotFoundException e) {
			return false;
		}
		return true;
	}

}

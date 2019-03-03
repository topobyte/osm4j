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

package de.topobyte.osm4j.osc;

import java.io.IOException;
import java.util.List;

import com.slimjars.dist.gnu.trove.iterator.TLongIterator;
import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.osc.dynsax.OsmChangeHandler;

public class CompletenessChecker implements OsmChangeHandler
{

	private TLongSet nodeIds = new TLongHashSet();
	private TLongSet wayIds = new TLongHashSet();
	private TLongSet relationIds = new TLongHashSet();

	@Override
	public void handle(OsmChange change) throws IOException
	{
		InMemoryListDataSet data = change.getElements();
		System.out.println(
				String.format("change: %s, %d nodes, %d ways, %d relations",
						change.getType(), data.getNodes().size(),
						data.getWays().size(), data.getRelations().size()));

		add(data.getNodes(), nodeIds);
		add(data.getWays(), wayIds);
		add(data.getRelations(), relationIds);

		if (!data.getWays().isEmpty()) {
			checkWays(data.getWays());
		}

		if (!data.getRelations().isEmpty()) {
			checkRelations(data.getRelations());
		}
	}

	private void add(List<? extends OsmEntity> objects, TLongSet set)
	{
		for (OsmEntity object : objects) {
			set.add(object.getId());
		}
	}

	private void checkWays(List<OsmWay> ways)
	{
		for (OsmWay way : ways) {
			checkWay(way);
		}
	}

	private void checkRelations(List<OsmRelation> relations)
	{
		for (OsmRelation relation : relations) {
			checkRelation(relation);
		}
	}

	private void checkWay(OsmWay way)
	{
		TLongList ids = OsmModelUtil.nodesAsList(way);
		TLongIterator iterator = ids.iterator();
		while (iterator.hasNext()) {
			long id = iterator.next();
			if (!nodeIds.contains(id)) {
				System.out.println("missing node: " + id);
			}
		}
	}

	private void checkRelation(OsmRelation relation)
	{
		List<OsmRelationMember> members = OsmModelUtil.membersAsList(relation);
		for (OsmRelationMember member : members) {
			EntityType type = member.getType();
			TLongSet set = set(type);
			if (!set.contains(member.getId())) {
				System.out.println(String.format("missing member: %s %d", type,
						member.getId()));
			}
		}
	}

	private TLongSet set(EntityType type)
	{
		switch (type) {
		default:
		case Node:
			return nodeIds;
		case Relation:
			return wayIds;
		case Way:
			return relationIds;
		}
	}

	@Override
	public void complete() throws IOException
	{
		System.out.println("complete");
	}

}

// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.extracts.query;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;

public class RelationSelector
{

	public InMemoryListDataSet select(RelationFilter filter,
			InMemoryListDataSet data)
	{
		InMemoryListDataSet result = new InMemoryListDataSet();
		List<OsmRelation> resultRelations = result.getRelations();

		List<OsmRelation> relations = data.getRelations();
		for (OsmRelation relation : relations) {
			if (filter.take(relation)) {
				resultRelations.add(relation);
			}
		}

		if (resultRelations.size() == relations.size()) {
			// We selected all relations anyway, hence no need to perform
			// additional work
			return result;
		}

		// Collect all relations that are referenced by any of the preselected
		// relations, also transitively.

		// all ids of the relations in the result set so far
		TLongSet ids = new TLongHashSet();
		for (OsmRelation relation : resultRelations) {
			ids.add(relation.getId());
		}

		Queue<OsmRelation> rQueue = new ArrayDeque<>(resultRelations);
		TLongSet idQueue = new TLongHashSet();

		while (!rQueue.isEmpty()) {
			OsmRelation next = rQueue.remove();
			for (OsmRelationMember member : OsmModelUtil.membersAsList(next)) {
				if (member.getType() != EntityType.Relation) {
					continue;
				}
				long id = member.getId();
				if (ids.contains(id) || idQueue.contains(id)) {
					continue;
				}
				idQueue.add(id);
			}
			for (long id : idQueue.toArray()) {
				try {
					idQueue.remove(id);
					OsmRelation relation = data.getRelation(id);
					resultRelations.add(relation);
					ids.add(relation.getId());
					rQueue.add(relation);
				} catch (EntityNotFoundException e) {
					continue;
				}
			}
		}

		return result;
	}

}

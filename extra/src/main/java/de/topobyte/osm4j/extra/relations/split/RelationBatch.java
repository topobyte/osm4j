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

package de.topobyte.osm4j.extra.relations.split;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmRelation;

class RelationBatch
{

	private int maxMembers;

	private List<OsmRelation> relations = new ArrayList<>();
	private int members = 0;

	RelationBatch(int maxMembers)
	{
		this.maxMembers = maxMembers;
	}

	void clear()
	{
		relations.clear();
		members = 0;
	}

	boolean fits(OsmRelation relation)
	{
		if (relations.isEmpty()) {
			return true;
		}
		if (members + relation.getNumberOfMembers() <= maxMembers) {
			return true;
		}
		return false;
	}

	void add(OsmRelation relation)
	{
		relations.add(relation);
		members += relation.getNumberOfMembers();
	}

	List<OsmRelation> getRelations()
	{
		return relations;
	}

}

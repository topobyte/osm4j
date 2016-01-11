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

package de.topobyte.osm4j.extra.relations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmRelation;

public class RelationGroupSingle implements RelationGroup
{

	private OsmRelation relation;

	public RelationGroupSingle(OsmRelation relation)
	{
		this.relation = relation;
	}

	@Override
	public long getId()
	{
		return relation.getId();
	}

	@Override
	public Collection<OsmRelation> getRelations()
	{
		List<OsmRelation> list = new ArrayList<>(1);
		list.add(relation);
		return list;
	}

}

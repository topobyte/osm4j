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

package de.topobyte.osm4j.core.model.impl;

import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;

public class Relation extends Entity implements OsmRelation
{

	private final List<? extends OsmRelationMember> members;

	public Relation(long id, List<? extends OsmRelationMember> members)
	{
		super(id, null);
		this.members = members;
	}

	public Relation(long id, List<? extends OsmRelationMember> members,
			OsmMetadata metadata)
	{
		super(id, metadata);
		this.members = members;
	}

	public Relation(long id, List<? extends OsmRelationMember> members,
			List<? extends OsmTag> tags)
	{
		this(id, members, tags, null);
	}

	public Relation(long id, List<? extends OsmRelationMember> members,
			List<? extends OsmTag> tags, OsmMetadata metadata)
	{
		super(id, tags, metadata);
		this.members = members;
	}

	public List<? extends OsmRelationMember> getMembers()
	{
		return members;
	}

	@Override
	public int getNumberOfMembers()
	{
		return members.size();
	}

	@Override
	public OsmRelationMember getMember(int n)
	{
		return members.get(n);
	}

}

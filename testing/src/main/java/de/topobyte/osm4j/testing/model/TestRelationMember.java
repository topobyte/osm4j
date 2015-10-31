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

package de.topobyte.osm4j.testing.model;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;

public class TestRelationMember implements OsmRelationMember
{

	private long id;
	private EntityType type;
	private String role;

	public TestRelationMember(long id, EntityType type, String role)
	{
		this.id = id;
		this.type = type;
		this.role = role;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public EntityType getType()
	{
		return type;
	}

	@Override
	public String getRole()
	{
		return role;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public void setType(EntityType type)
	{
		this.type = type;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

}

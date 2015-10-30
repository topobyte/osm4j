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

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;

public class Entity implements OsmEntity
{

	private long id;
	private List<? extends OsmTag> tags;
	private OsmMetadata metadata;

	public Entity(long id, OsmMetadata metadata)
	{
		this.id = id;
		this.metadata = metadata;
		tags = new ArrayList<OsmTag>();
	}

	public Entity(long id, List<? extends OsmTag> tags, OsmMetadata metadata)
	{
		this.id = id;
		this.tags = tags;
		this.metadata = metadata;
	}

	@Override
	public long getId()
	{
		return id;
	}

	public List<? extends OsmTag> getTags()
	{
		return tags;
	}

	public void setTags(List<? extends OsmTag> tags)
	{
		this.tags = tags;
	}

	@Override
	public int getNumberOfTags()
	{
		return tags.size();
	}

	@Override
	public OsmTag getTag(int n)
	{
		return tags.get(n);
	}

	@Override
	public OsmMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(OsmMetadata metadata)
	{
		this.metadata = metadata;
	}

}

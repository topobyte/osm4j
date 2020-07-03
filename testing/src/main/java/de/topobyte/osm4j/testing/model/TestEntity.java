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

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmEntity;

public class TestEntity implements OsmEntity
{

	private long id;
	private List<TestTag> tags;
	private TestMetadata metadata;

	public TestEntity(long id, TestMetadata metadata)
	{
		this.id = id;
		this.metadata = metadata;
		tags = new ArrayList<>();
	}

	public TestEntity(long id, List<TestTag> tags, TestMetadata metadata)
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

	public List<TestTag> getTags()
	{
		return tags;
	}

	public void setTags(List<TestTag> tags)
	{
		this.tags = tags;
	}

	@Override
	public int getNumberOfTags()
	{
		return tags.size();
	}

	@Override
	public TestTag getTag(int n)
	{
		return tags.get(n);
	}

	@Override
	public TestMetadata getMetadata()
	{
		return metadata;
	}

	public void setMetadata(TestMetadata metadata)
	{
		this.metadata = metadata;
	}

}

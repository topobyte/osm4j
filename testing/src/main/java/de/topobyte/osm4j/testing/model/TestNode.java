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

import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmNode;

public class TestNode extends TestEntity implements OsmNode
{

	private double lon;
	private double lat;

	public TestNode(long id, double lon, double lat)
	{
		super(id, null);
		this.lon = lon;
		this.lat = lat;
	}

	public TestNode(long id, double lon, double lat, TestMetadata metadata)
	{
		super(id, metadata);
		this.lon = lon;
		this.lat = lat;
	}

	public TestNode(long id, double lon, double lat, List<TestTag> tags)
	{
		this(id, lon, lat, tags, null);
	}

	public TestNode(long id, double lon, double lat, List<TestTag> tags,
			TestMetadata metadata)
	{
		super(id, tags, metadata);
		this.lon = lon;
		this.lat = lat;
	}

	@Override
	public double getLongitude()
	{
		return lon;
	}

	@Override
	public double getLatitude()
	{
		return lat;
	}

	public void setLongitude(double lon)
	{
		this.lon = lon;
	}

	public void setLatitude(double lat)
	{
		this.lat = lat;
	}

}

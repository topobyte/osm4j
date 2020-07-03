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

import de.topobyte.osm4j.core.model.iface.OsmMetadata;

public class TestMetadata implements OsmMetadata
{

	private int version;
	private long timestamp;
	private long uid;
	private String user;
	private long changeset;
	private boolean visible = true;

	public TestMetadata(int version, long timestamp, long uid, String user,
			long changeset)
	{
		this.version = version;
		this.timestamp = timestamp;
		this.uid = uid;
		this.user = user;
		this.changeset = changeset;
	}

	public TestMetadata(int version, long timestamp, long uid, String user,
			long changeset, boolean visible)
	{
		this(version, timestamp, uid, user, changeset);
		this.visible = visible;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	@Override
	public long getTimestamp()
	{
		return timestamp;
	}

	@Override
	public long getUid()
	{
		return uid;
	}

	@Override
	public String getUser()
	{
		return user;
	}

	@Override
	public long getChangeset()
	{
		return changeset;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

}

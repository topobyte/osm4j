// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.changeset;

import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmTag;

public class OsmChangeset
{

	private long id;
	private long createdAt;
	private long closedAt;
	private boolean open;
	private int numChanges;
	private String user;
	private long uid;
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;
	private int commentsCount;

	private List<? extends OsmTag> tags;

	public OsmChangeset(long id, long createdAt, long closedAt, boolean open,
			int numChanges, String user, long uid, double minLat, double maxLat,
			double minLon, double maxLon, int commentsCount)
	{
		this.id = id;
		this.createdAt = createdAt;
		this.closedAt = closedAt;
		this.open = open;
		this.numChanges = numChanges;
		this.user = user;
		this.uid = uid;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.commentsCount = commentsCount;
	}

	public long getId()
	{
		return id;
	}

	public long getCreatedAt()
	{
		return createdAt;
	}

	public long getClosedAt()
	{
		return closedAt;
	}

	public boolean isOpen()
	{
		return open;
	}

	public int getNumChanges()
	{
		return numChanges;
	}

	public String getUser()
	{
		return user;
	}

	public long getUid()
	{
		return uid;
	}

	public double getMinLat()
	{
		return minLat;
	}

	public double getMaxLat()
	{
		return maxLat;
	}

	public double getMinLon()
	{
		return minLon;
	}

	public double getMaxLon()
	{
		return maxLon;
	}

	public int getCommentsCount()
	{
		return commentsCount;
	}

	public List<? extends OsmTag> getTags()
	{
		return tags;
	}

	public void setTags(List<? extends OsmTag> tags)
	{
		this.tags = tags;
	}

}

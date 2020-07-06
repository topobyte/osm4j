// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.waydb.osmmodel;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.diskstorage.waydb.AbstractWayRecord;

/**
 * An implementation of OsmWay using waydb's way entities as base and
 * redirecting to nodedb's nodes as waynodes.
 *
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayImpl implements OsmWay
{

	AbstractWayRecord way;
	List<String> tagKeys = new ArrayList<>();

	/**
	 * Create an OsmWay from a waydb way that provides nodes from a nodedb.
	 * 
	 * @param nodeDb
	 *            the database to look nodes up from
	 * @param way
	 *            the way that is the base of this way
	 */
	public WayImpl(AbstractWayRecord way)
	{
		this.way = way;
		for (String key : way.getTags().keySet()) {
			tagKeys.add(key);
		}
	}

	@Override
	public long getId()
	{
		return way.getId();
	}

	@Override
	public int getNumberOfTags()
	{
		return way.getTags().size();
	}

	@Override
	public OsmTag getTag(final int n)
	{
		return new OsmTag() {

			@Override
			public String getKey()
			{
				return tagKeys.get(n);
			}

			@Override
			public String getValue()
			{
				return way.getTags().get(tagKeys.get(n));
			}
		};
	}

	@Override
	public int getNumberOfNodes()
	{
		return way.getNodeIds().size();
	}

	@Override
	public OsmMetadata getMetadata()
	{
		return null;
	}

	@Override
	public long getNodeId(int index)
	{
		return way.getNodeIds().get(index);
	}

}

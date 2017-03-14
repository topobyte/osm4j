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

import gnu.trove.list.TLongList;

import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class Way extends Entity implements OsmWay
{

	private final TLongList nodes;

	public Way(long id, TLongList nodes)
	{
		super(id, null);
		this.nodes = nodes;
	}

	public Way(long id, TLongList nodes, OsmMetadata metadata)
	{
		super(id, metadata);
		this.nodes = nodes;
	}

	public Way(long id, TLongList nodes, List<? extends OsmTag> tags)
	{
		this(id, nodes, tags, null);
	}

	public Way(long id, TLongList nodes, List<? extends OsmTag> tags,
			OsmMetadata metadata)
	{
		super(id, tags, metadata);
		this.nodes = nodes;
	}

	public TLongList getNodes()
	{
		return nodes;
	}

	@Override
	public int getNumberOfNodes()
	{
		return nodes.size();
	}

	@Override
	public long getNodeId(int n)
	{
		return nodes.get(n);
	}

}

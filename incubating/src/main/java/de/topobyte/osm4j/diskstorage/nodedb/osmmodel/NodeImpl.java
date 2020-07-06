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

package de.topobyte.osm4j.diskstorage.nodedb.osmmodel;

import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.diskstorage.nodedb.DbNode;

/**
 * An OsmNode implementation for nodedb's nodes.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class NodeImpl implements OsmNode
{

	private DbNode node;

	/**
	 * Create a new NodeImpl wrapping a database node.
	 * 
	 * @param node
	 *            the node to wrap
	 */
	public NodeImpl(DbNode node)
	{
		this.node = node;
	}

	@Override
	public long getId()
	{
		return node.getId();
	}

	@Override
	public int getNumberOfTags()
	{
		return 0;
	}

	@Override
	public OsmTag getTag(int n)
	{
		return null;
	}

	@Override
	public double getLongitude()
	{
		return node.getLon();
	}

	@Override
	public double getLatitude()
	{
		return node.getLat();
	}

	@Override
	public OsmMetadata getMetadata()
	{
		return null;
	}

}

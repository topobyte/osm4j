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

package de.topobyte.osm4j.extra.nodearray;

import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class NodeArrayEntityProvider implements OsmEntityProvider
{

	private final static String MESSAGE_NON_NODE = "This provider contains only nodes";

	private NodeArray nodeArray;

	public NodeArrayEntityProvider(NodeArray nodeArray)
	{
		this.nodeArray = nodeArray;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		try {
			return nodeArray.get(id);
		} catch (IOException e) {
			throw new EntityNotFoundException(
					"Node not found due to IOException " + e.getMessage());
		}
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException(MESSAGE_NON_NODE);
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException(MESSAGE_NON_NODE);
	}

}

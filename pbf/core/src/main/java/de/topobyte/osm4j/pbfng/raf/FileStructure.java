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

package de.topobyte.osm4j.pbfng.raf;

import de.topobyte.osm4j.core.model.iface.EntityType;

public class FileStructure
{

	private Interval blocksNodes = null;
	private Interval blocksWays = null;
	private Interval blocksRelations = null;

	public FileStructure(Interval blocksNodes, Interval blocksWays,
			Interval blocksRelations)
	{
		this.blocksNodes = blocksNodes;
		this.blocksWays = blocksWays;
		this.blocksRelations = blocksRelations;
	}

	public boolean hasType(EntityType type)
	{
		switch (type) {
		case Node:
			return hasNodes();
		case Way:
			return hasWays();
		case Relation:
			return hasRelations();
		default:
			return false;
		}
	}

	public Interval getBlocks(EntityType type)
	{
		switch (type) {
		case Node:
			return blocksNodes;
		case Way:
			return blocksWays;
		case Relation:
			return blocksRelations;
		default:
			return null;
		}
	}

	public boolean hasNodes()
	{
		return blocksNodes != null;
	}

	public boolean hasWays()
	{
		return blocksWays != null;
	}

	public boolean hasRelations()
	{
		return blocksRelations != null;
	}

	public Interval getBlocksNodes()
	{
		return blocksNodes;
	}

	public void setBlocksNodes(Interval blocksNodes)
	{
		this.blocksNodes = blocksNodes;
	}

	public Interval getBlocksWays()
	{
		return blocksWays;
	}

	public void setBlocksWays(Interval blocksWays)
	{
		this.blocksWays = blocksWays;
	}

	public Interval getBlocksRelations()
	{
		return blocksRelations;
	}

	public void setBlocksRelations(Interval blocksRelations)
	{
		this.blocksRelations = blocksRelations;
	}

}

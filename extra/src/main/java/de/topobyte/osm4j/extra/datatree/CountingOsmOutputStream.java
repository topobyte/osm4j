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

package de.topobyte.osm4j.extra.datatree;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class CountingOsmOutputStream implements OsmOutputStream
{

	private OsmOutputStream output;

	private long numNodes = 0;
	private long numWays = 0;
	private long numRelations = 0;

	public CountingOsmOutputStream(OsmOutputStream output)
	{
		this.output = output;
	}

	public long getNumNodes()
	{
		return numNodes;
	}

	public long getNumWays()
	{
		return numWays;
	}

	public long getNumRelations()
	{
		return numRelations;
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		output.write(bounds);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		output.write(node);
		numNodes++;
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		output.write(way);
		numWays++;
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		output.write(relation);
		numRelations++;
	}

	@Override
	public void complete() throws IOException
	{
		output.complete();
	}

}

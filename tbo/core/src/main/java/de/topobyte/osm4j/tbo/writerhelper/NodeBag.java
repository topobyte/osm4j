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

package de.topobyte.osm4j.tbo.writerhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public class NodeBag extends EntityBag
{

	private List<OsmNode> nodes;

	public NodeBag(int batchSize)
	{
		nodes = new ArrayList<OsmNode>(batchSize);
	}

	public void put(OsmNode node)
	{
		nodes.add(node);
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		super.write(writer, nodes);
		for (OsmNode node : nodes) {
			writeIds(writer, node);
		}
		for (OsmNode node : nodes) {
			writeCoords(writer, node);
		}
		for (OsmNode node : nodes) {
			writeTags(writer, node);
		}
	}

	private long idOffset = 0;

	private long latOffset = 0;
	private long lonOffset = 0;

	private void writeIds(CompactWriter writer, OsmNode node)
			throws IOException
	{
		long id = node.getId();

		writer.writeVariableLengthSignedInteger(id - idOffset);
		idOffset = id;
	}

	private void writeCoords(CompactWriter writer, OsmNode node)
			throws IOException
	{
		double lat = node.getLatitude();
		double lon = node.getLongitude();
		long mlat = toLong(lat);
		long mlon = toLong(lon);

		writer.writeVariableLengthSignedInteger(mlat - latOffset);
		writer.writeVariableLengthSignedInteger(mlon - lonOffset);
		latOffset = mlat;
		lonOffset = mlon;
	}

	private long toLong(double degrees)
	{
		return (long) (degrees / .0000001);
	}

	public void clear()
	{
		nodes.clear();
		idOffset = 0;
		latOffset = 0;
		lonOffset = 0;
	}

}

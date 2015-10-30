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

import de.topobyte.compactio.CompactWriter;
import de.topobyte.osm4j.core.model.iface.OsmNode;

public class NodeBatch extends EntityBatch<OsmNode>
{

	public NodeBatch(boolean writeMetadata)
	{
		super(writeMetadata);
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		writeTagStringPool(writer);

		for (OsmNode node : elements) {
			writeIds(writer, node);
		}
		for (OsmNode node : elements) {
			writeCoords(writer, node);
		}
		for (OsmNode node : elements) {
			writeTags(writer, node);
		}

		writeMetadata(writer);
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

	@Override
	public void clear()
	{
		super.clear();
		idOffset = 0;
		latOffset = 0;
		lonOffset = 0;
	}

}

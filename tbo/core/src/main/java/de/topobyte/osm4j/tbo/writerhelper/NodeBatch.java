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
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.tbo.ByteArrayOutputStream;

public class NodeBatch extends EntityBatch<OsmNode>
{

	public NodeBatch(boolean writeMetadata)
	{
		super(writeMetadata);
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CompactWriter bwriter = new OutputStreamCompactWriter(baos);

		writeTagStringPool(bwriter);
		writeAndReset(writer, baos);

		writeIds(bwriter);
		writeAndReset(writer, baos);

		writeCoords(bwriter);
		writeAndReset(writer, baos);

		writeTags(bwriter);
		writeAndReset(writer, baos);

		writeMetadata(bwriter);
		writeAndReset(writer, baos);
	}

	private long latOffset = 0;
	private long lonOffset = 0;

	private void writeCoords(CompactWriter writer) throws IOException
	{
		for (OsmNode node : elements) {
			double lat = node.getLatitude();
			double lon = node.getLongitude();
			long mlat = toLong(lat);
			long mlon = toLong(lon);

			writer.writeVariableLengthSignedInteger(mlat - latOffset);
			writer.writeVariableLengthSignedInteger(mlon - lonOffset);
			latOffset = mlat;
			lonOffset = mlon;
		}
	}

	private long toLong(double degrees)
	{
		return (long) (degrees / .0000001);
	}

	@Override
	public void clear()
	{
		super.clear();
		latOffset = 0;
		lonOffset = 0;
	}

}

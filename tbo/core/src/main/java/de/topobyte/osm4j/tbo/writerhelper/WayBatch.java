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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class WayBatch extends EntityBatch<OsmWay>
{

	public WayBatch(boolean writeMetadata)
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

		writeNodes(bwriter);
		writeAndReset(writer, baos);

		writeTags(bwriter);
		writeAndReset(writer, baos);

		writeMetadata(bwriter);
		writeAndReset(writer, baos);
	}

	private long nidOffset = 0;

	private void writeNodes(CompactWriter writer) throws IOException
	{
		for (OsmWay way : elements) {
			int nNodes = way.getNumberOfNodes();

			writer.writeVariableLengthUnsignedInteger(nNodes);
			for (int i = 0; i < nNodes; i++) {
				long nid = way.getNodeId(i);
				writer.writeVariableLengthSignedInteger(nid - nidOffset);
				nidOffset = nid;
			}
		}
	}

	@Override
	public void clear()
	{
		super.clear();
		nidOffset = 0;
	}

}

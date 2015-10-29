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

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public class WayBag extends EntityBag<OsmWay>
{

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		super.writeStringPool(writer);
		for (OsmWay way : elements) {
			write(writer, way);
		}
	}

	private long idOffset = 0;
	private long nidOffset = 0;

	private void write(CompactWriter writer, OsmWay way) throws IOException
	{
		long id = way.getId();
		int nNodes = way.getNumberOfNodes();

		writer.writeVariableLengthSignedInteger(id - idOffset);
		idOffset = id;

		writer.writeVariableLengthUnsignedInteger(nNodes);
		for (int i = 0; i < nNodes; i++) {
			long nid = way.getNodeId(i);
			writer.writeVariableLengthSignedInteger(nid - nidOffset);
			nidOffset = nid;
		}

		writeTags(writer, way);
	}

	@Override
	public void clear()
	{
		super.clear();
		idOffset = 0;
		nidOffset = 0;
	}

}

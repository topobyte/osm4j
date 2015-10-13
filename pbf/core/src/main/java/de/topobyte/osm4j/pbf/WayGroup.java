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
//
//
// This files is based on a file from Osmosis. The original file contained this
// copyright notice:
//
// This software is released into the Public Domain. See copying.txt for details.
//
//
// And the mentioned copying.txt states:
//
// Osmosis is placed into the public domain and where this is not legally
// possible everybody is granted a perpetual, irrevocable license to use
// this work for any purpose whatsoever.
//
// DISCLAIMERS
// By making Osmosis publicly available, it is hoped that users will find the
// software useful. However:
//   * Osmosis comes without any warranty, to the extent permitted by
//     applicable law.
//   * Unless required by applicable law, no liability will be accepted by
// the authors and distributors of this software for any damages caused
// as a result of its use.

package de.topobyte.osm4j.pbf;

import crosby.binary.BinarySerializer;
import crosby.binary.BinarySerializer.PrimGroupWriterInterface;
import crosby.binary.Osmformat;
import crosby.binary.StringTable;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;

class WayGroup extends Prim<OsmWay> implements PrimGroupWriterInterface
{

	public WayGroup(boolean writeMetadata)
	{
		super(writeMetadata);
	}

	@Override
	public Osmformat.PrimitiveGroup serialize(BinarySerializer serializer)
	{
		if (contents.size() == 0) {
			return null;
		}

		StringTable stable = serializer.getStringTable();
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();
		for (OsmWay way : contents) {
			Osmformat.Way.Builder bi = Osmformat.Way.newBuilder();
			bi.setId(way.getId());
			long lastid = 0;
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				long id = way.getNodeId(k);
				bi.addRefs(id - lastid);
				lastid = id;
			}
			for (int k = 0; k < way.getNumberOfTags(); k++) {
				OsmTag t = way.getTag(k);
				bi.addKeys(stable.getIndex(t.getKey()));
				bi.addVals(stable.getIndex(t.getValue()));
			}
			bi.setInfo(serializeMetadata(way, serializer));
			builder.addWays(bi);
		}
		return builder.build();
	}

}

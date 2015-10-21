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
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmTag;

class NodeGroup extends Prim<OsmNode> implements PrimGroupWriterInterface
{

	private final boolean useDense;

	public NodeGroup(boolean useDense, boolean writeMetadata)
	{
		super(writeMetadata);
		this.useDense = useDense;
	}

	@Override
	public Osmformat.PrimitiveGroup serialize(BinarySerializer serializer)
	{
		if (useDense) {
			return serializeDense(serializer);
		} else {
			return serializeNonDense(serializer);
		}
	}

	/**
	 * Serialize all nodes in the 'dense' format.
	 * 
	 * @param serializer
	 */
	public Osmformat.PrimitiveGroup serializeDense(BinarySerializer serializer)
	{
		if (contents.size() == 0) {
			return null;
		}
		// System.out.format("%d Dense   ",nodes.size());
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();

		long lastlat = 0, lastlon = 0, lastid = 0;
		Osmformat.DenseNodes.Builder bi = Osmformat.DenseNodes.newBuilder();
		boolean doesBlockHaveTags = false;
		// Does anything in this block have tags?
		for (OsmNode node : contents) {
			doesBlockHaveTags = doesBlockHaveTags
					|| (node.getNumberOfTags() != 0);
		}

		if (writeMetadata) {
			Osmformat.DenseInfo.Builder bdi = Osmformat.DenseInfo.newBuilder();
			serializeMetadataDense(bdi, contents, serializer);
			bi.setDenseinfo(bdi);
		}

		for (OsmNode node : contents) {
			long id = node.getId();
			int lat = serializer.mapDegrees(node.getLatitude());
			int lon = serializer.mapDegrees(node.getLongitude());
			bi.addId(id - lastid);
			lastid = id;
			bi.addLon(lon - lastlon);
			lastlon = lon;
			bi.addLat(lat - lastlat);
			lastlat = lat;

			// Then we must include tag information.
			if (doesBlockHaveTags) {
				StringTable stable = serializer.getStringTable();
				for (int k = 0; k < node.getNumberOfTags(); k++) {
					OsmTag t = node.getTag(k);
					bi.addKeysVals(stable.getIndex(t.getKey()));
					bi.addKeysVals(stable.getIndex(t.getValue()));
				}
				bi.addKeysVals(0); // Add delimiter.
			}
		}
		builder.setDense(bi);
		return builder.build();
	}

	/**
	 * Serialize all nodes in the non-dense format.
	 * 
	 * @param serializer
	 * 
	 * @param parentbuilder
	 *            Add to this PrimitiveBlock.
	 */
	public Osmformat.PrimitiveGroup serializeNonDense(
			BinarySerializer serializer)
	{
		if (contents.size() == 0) {
			return null;
		}
		// System.out.format("%d Nodes   ",nodes.size());
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();
		StringTable stable = serializer.getStringTable();
		for (OsmNode node : contents) {
			long id = node.getId();
			int lat = serializer.mapDegrees(node.getLatitude());
			int lon = serializer.mapDegrees(node.getLongitude());
			Osmformat.Node.Builder bi = Osmformat.Node.newBuilder();
			bi.setId(id);
			bi.setLon(lon);
			bi.setLat(lat);
			for (int k = 0; k < node.getNumberOfTags(); k++) {
				OsmTag t = node.getTag(k);
				bi.addKeys(stable.getIndex(t.getKey()));
				bi.addVals(stable.getIndex(t.getValue()));
			}
			if (writeMetadata && node.getMetadata() != null) {
				bi.setInfo(serializeMetadata(node, serializer));
			}
			builder.addNodes(bi);
		}
		return builder.build();
	}

}

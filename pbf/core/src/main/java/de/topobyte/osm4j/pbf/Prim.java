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

import java.util.ArrayList;
import java.util.List;

import crosby.binary.BinarySerializer;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseInfo;
import crosby.binary.Osmformat.Info;
import crosby.binary.StringTable;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;

abstract class Prim<T extends OsmEntity>
{

	private boolean writeMetadata = true;

	public Prim(boolean writeMetadata)
	{
		this.writeMetadata = writeMetadata;
	}

	/** Queue that tracks the list of all primitives. */
	ArrayList<T> contents = new ArrayList<T>();

	/**
	 * Add to the queue.
	 * 
	 * @param item
	 *            The entity to add
	 */
	public void add(T item)
	{
		contents.add(item);
	}

	/** Add all of the tags of all entities in the queue to the stringtable. */
	public void addStringsToStringtable(StringTable stable)
	{
		for (T i : contents) {
			for (int k = 0; k < i.getNumberOfTags(); k++) {
				OsmTag tag = i.getTag(k);
				stable.incr(tag.getKey());
				stable.incr(tag.getValue());
			}
			if (writeMetadata) {
				OsmMetadata metadata = i.getMetadata();
				if (metadata != null) {
					String user = metadata.getUser();
					if (user != null) {
						stable.incr(user);
					}
				}
			}
		}
	}

	protected Info.Builder serializeMetadata(OsmEntity entity,
			BinarySerializer serializer)
	{
		StringTable stable = serializer.getStringTable();
		Osmformat.Info.Builder b = Osmformat.Info.newBuilder();
		if (writeMetadata) {
			OsmMetadata metadata = entity.getMetadata();
			if (metadata == null) {
				return b;
			}
			if (metadata.getUid() >= 0) {
				b.setUid((int) metadata.getUid());
				b.setUserSid(stable.getIndex(metadata.getUser()));
			}
			b.setTimestamp((int) (metadata.getTimestamp() / serializer.date_granularity));
			b.setVersion(metadata.getVersion());
			b.setChangeset(metadata.getChangeset());
		}
		return b;
	}

	public void serializeMetadataDense(DenseInfo.Builder b,
			List<? extends OsmEntity> entities, BinarySerializer serializer)
	{
		if (!writeMetadata) {
			return;
		}

		long lasttimestamp = 0, lastchangeset = 0;
		int lastuserSid = 0, lastuid = 0;
		StringTable stable = serializer.getStringTable();
		for (OsmEntity e : entities) {
			OsmMetadata metadata = e.getMetadata();
			if (metadata == null) {
				continue;
			}
			int uid = (int) metadata.getUid();
			int userSid = stable.getIndex(metadata.getUser());
			int timestamp = (int) (metadata.getTimestamp() / serializer.date_granularity);
			int version = metadata.getVersion();
			long changeset = metadata.getChangeset();

			b.addVersion(version);
			b.addTimestamp(timestamp - lasttimestamp);
			lasttimestamp = timestamp;
			b.addChangeset(changeset - lastchangeset);
			lastchangeset = changeset;
			b.addUid(uid - lastuid);
			lastuid = uid;
			b.addUserSid(userSid - lastuserSid);
			lastuserSid = userSid;
		}
	}

}

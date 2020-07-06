// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.waydb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.diskstorage.HighLevelInputStream;
import de.topobyte.osm4j.diskstorage.HighLevelOutputStream;
import de.topobyte.osm4j.diskstorage.vardb.Record;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class WayRecordWithTags extends AbstractWayRecord
{

	private long id;
	private TLongList nodeIds = new TLongArrayList();
	private Map<String, String> tags = new HashMap<>();

	/**
	 * @param id
	 *            the id of the way.
	 */
	public WayRecordWithTags(long id)
	{
		this.id = id;
	}

	/**
	 * @param id
	 *            the id of the way.
	 * @param nodeIds
	 *            the ids of the nodes.
	 * @param tags
	 *            the tags of the entity.
	 * 
	 */
	public WayRecordWithTags(long id, TLongList nodeIds,
			Map<String, String> tags)
	{
		this.id = id;
		this.nodeIds = nodeIds;
		this.tags = tags;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public int getNumberOfBytes()
	{
		int nbytes = nodeIds.size() * 8; // one long per node
		nbytes += 2; // short. number of nodes.
		nbytes += 2; // short. number of tags.
		for (String key : tags.keySet()) {
			String val = tags.get(key);
			try {
				nbytes += 2 + key.getBytes("UTF-8").length;
				nbytes += 2 + val.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				// do nothing here
			}
		}
		return nbytes;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException
	{
		HighLevelOutputStream hlos = new HighLevelOutputStream(stream);
		hlos.writeShort(tags.size());
		for (String key : tags.keySet()) {
			String val = tags.get(key);
			hlos.writeString(key);
			hlos.writeString(val);
		}
		hlos.writeShort(nodeIds.size());
		for (long nodeId : nodeIds.toArray()) {
			hlos.writeLong(nodeId);
		}
		hlos.close();
	}

	@Override
	public Record fromBytes(long wayId, InputStream stream, int nbytes)
			throws IOException
	{
		WayRecordWithTags wayRecord = new WayRecordWithTags(wayId);
		HighLevelInputStream hlis = new HighLevelInputStream(stream);
		int ntags = hlis.readShort();
		for (int i = 0; i < ntags; i++) {
			String key = hlis.readString();
			String val = hlis.readString();
			wayRecord.tags.put(key, val);
		}
		int nnodes = hlis.readShort();
		for (int i = 0; i < nnodes; i++) {
			long nodeId = hlis.readLong();
			wayRecord.nodeIds.add(nodeId);
		}
		hlis.close();
		return wayRecord;
	}

	/**
	 * @return this way's nodes' ids.
	 */
	@Override
	public TLongList getNodeIds()
	{
		return nodeIds;
	}

	/**
	 * @return this way's tags as a map.
	 */
	@Override
	public Map<String, String> getTags()
	{
		return tags;
	}

}

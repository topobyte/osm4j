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
public class WayRecord extends AbstractWayRecord
{

	private long id;
	private TLongList nodeIds = new TLongArrayList();

	/**
	 * @param id
	 *            the id of the way.
	 */
	public WayRecord(long id)
	{
		this.id = id;
	}

	/**
	 * @param id
	 *            the id of the way.
	 * @param nodeIds
	 *            the list of node ids.
	 * 
	 */
	public WayRecord(long id, TLongList nodeIds)
	{
		this.id = id;
		this.nodeIds = nodeIds;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public int getNumberOfBytes()
	{
		return nodeIds.size() * 8;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException
	{
		HighLevelOutputStream hlos = new HighLevelOutputStream(stream);
		for (long nodeId : nodeIds.toArray()) {
			hlos.writeLong(nodeId);
			// hlos.writeLong(Long.MAX_VALUE);
		}
		hlos.close();
	}

	@Override
	public Record fromBytes(long wayId, InputStream stream, int nbytes)
			throws IOException
	{
		WayRecord wayRecord = new WayRecord(wayId);
		HighLevelInputStream hlis = new HighLevelInputStream(stream);
		for (int i = 0; i < nbytes / 8; i++) {
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

	@Override
	public Map<String, String> getTags()
	{
		return new HashMap<>();
	}

}

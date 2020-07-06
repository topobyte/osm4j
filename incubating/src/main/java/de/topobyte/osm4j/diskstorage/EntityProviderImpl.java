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

package de.topobyte.osm4j.diskstorage;

import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.diskstorage.nodedb.DbNode;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.nodedb.osmmodel.NodeImpl;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.AbstractWayRecord;
import de.topobyte.osm4j.diskstorage.waydb.osmmodel.WayImpl;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class EntityProviderImpl implements OsmEntityProvider
{

	private final NodeDB nodeDB;
	private final VarDB<? extends AbstractWayRecord> wayDB;

	/**
	 * Create a new OsmEntityProvider implementation based on a NodeDB and a
	 * WayDB instance.
	 * 
	 * @param nodeDB
	 *            the node database.
	 * @param wayDB
	 *            the way database.
	 */
	public EntityProviderImpl(NodeDB nodeDB,
			VarDB<? extends AbstractWayRecord> wayDB)
	{
		this.nodeDB = nodeDB;
		this.wayDB = wayDB;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		DbNode dbNode;
		try {
			dbNode = nodeDB.find(id);
		} catch (IOException e) {
			throw new EntityNotFoundException("IO Error while looking up node");
		}
		if (dbNode == null) {
			throw new EntityNotFoundException("node not found in database");
		}
		NodeImpl nodeImpl = new NodeImpl(dbNode);
		return nodeImpl;
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		AbstractWayRecord dbWay;
		try {
			dbWay = wayDB.find(id);
		} catch (IOException e) {
			throw new EntityNotFoundException("IO Error while looking up way");
		}
		if (dbWay == null) {
			throw new EntityNotFoundException("way not found in database");
		}
		WayImpl wayImpl = new WayImpl(dbWay);
		return wayImpl;
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException("no relations in this provider");
	}

}

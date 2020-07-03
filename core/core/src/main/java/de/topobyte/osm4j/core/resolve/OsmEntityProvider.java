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

package de.topobyte.osm4j.core.resolve;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

/**
 * An implementation of this class is used as a DAO for basic OSM-types.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface OsmEntityProvider
{

	/**
	 * Get the node with the given id.
	 * 
	 * @param id
	 *            the node's id.
	 * @return the node.
	 * 
	 * @throws EntityNotFoundException
	 *             if the implementation cannot return this entity.
	 */
	public OsmNode getNode(long id) throws EntityNotFoundException;

	/**
	 * Get the way with the given id.
	 * 
	 * @param id
	 *            the way's id
	 * @return the way.
	 * 
	 * @throws EntityNotFoundException
	 *             if the implementation cannot return this entity.
	 */
	public OsmWay getWay(long id) throws EntityNotFoundException;

	/**
	 * Get the relation with the given id.
	 * 
	 * @param id
	 *            the relation's id.
	 * @return the relation.
	 * 
	 * @throws EntityNotFoundException
	 *             if the implementation cannot return this entity.
	 */
	public OsmRelation getRelation(long id) throws EntityNotFoundException;

}

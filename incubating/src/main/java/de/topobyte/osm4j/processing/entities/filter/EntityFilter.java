// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.processing.entities.filter;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public interface EntityFilter
{

	/**
	 * Filter nodes.
	 * 
	 * @param node
	 *            the node
	 * @return whether to process this node or not.
	 */
	public boolean filterNode(OsmNode node);

	/**
	 * Filter ways.
	 * 
	 * @param way
	 *            the way
	 * @return whether to process this way or not.
	 */
	public boolean filterWay(OsmWay way);

	/**
	 * Filter relations.
	 * 
	 * @param relation
	 *            the relation
	 * @return whether to process this relation or not.
	 */
	public boolean filterRelation(OsmRelation relation);

}

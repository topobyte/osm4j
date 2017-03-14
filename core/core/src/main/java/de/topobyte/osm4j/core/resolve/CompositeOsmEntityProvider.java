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

public class CompositeOsmEntityProvider implements OsmEntityProvider
{

	private OsmEntityProvider nodeProvider;
	private OsmEntityProvider wayProvider;
	private OsmEntityProvider relationProvider;

	public CompositeOsmEntityProvider(OsmEntityProvider nodeProvider,
			OsmEntityProvider wayProvider, OsmEntityProvider relationProvider)
	{
		this.nodeProvider = nodeProvider;
		this.wayProvider = wayProvider;
		this.relationProvider = relationProvider;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		if (nodeProvider == null) {
			throw new EntityNotFoundException("No node-provider supplied");
		}
		return nodeProvider.getNode(id);
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		if (wayProvider == null) {
			throw new EntityNotFoundException("No way-provider supplied");
		}
		return wayProvider.getWay(id);
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		if (relationProvider == null) {
			throw new EntityNotFoundException("No relation-provider supplied");
		}
		return relationProvider.getRelation(id);
	}

}

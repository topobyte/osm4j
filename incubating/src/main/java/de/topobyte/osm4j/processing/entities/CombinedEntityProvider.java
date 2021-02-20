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

package de.topobyte.osm4j.processing.entities;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.diskstorage.EntityProviderImpl;

public class CombinedEntityProvider implements OsmEntityProvider
{

	private EntityProviderImpl nodeWayProvider;
	private TLongObjectMap<OsmRelation> relations;
	private RelationStore relationStore;

	public CombinedEntityProvider(EntityProviderImpl nodeWayProvider,
			TLongObjectMap<OsmRelation> relations, RelationStore relationStore)
	{
		this.nodeWayProvider = nodeWayProvider;
		this.relations = relations;
		this.relationStore = relationStore;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		return nodeWayProvider.getNode(id);
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		return nodeWayProvider.getWay(id);
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		OsmRelation relation = relations.get(id);
		if (relation == null) {
			throw new EntityNotFoundException(
					"unable to locate relation for id: " + id);
		}
		return relationStore.getReplacement(relation);
	}

}

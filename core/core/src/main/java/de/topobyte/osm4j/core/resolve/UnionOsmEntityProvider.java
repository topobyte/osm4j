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

import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class UnionOsmEntityProvider implements OsmEntityProvider
{

	private List<OsmEntityProvider> providers;

	public UnionOsmEntityProvider(List<OsmEntityProvider> providers)
	{
		this.providers = providers;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		for (int i = 0; i < providers.size(); i++) {
			OsmEntityProvider provider = providers.get(i);
			try {
				return provider.getNode(id);
			} catch (EntityNotFoundException e) {
				continue;
			}
		}
		throw new EntityNotFoundException("unable to find node with id: " + id);
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		for (int i = 0; i < providers.size(); i++) {
			OsmEntityProvider provider = providers.get(i);
			try {
				return provider.getWay(id);
			} catch (EntityNotFoundException e) {
				continue;
			}
		}
		throw new EntityNotFoundException("unable to find way with id: " + id);
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		for (int i = 0; i < providers.size(); i++) {
			OsmEntityProvider provider = providers.get(i);
			try {
				return provider.getRelation(id);
			} catch (EntityNotFoundException e) {
				continue;
			}
		}
		throw new EntityNotFoundException("unable to find relation with id: "
				+ id);
	}

}

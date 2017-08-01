// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.extracts.query;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;
import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;

class RelationQueryBag
{

	OsmStreamOutput outRelations;

	TLongSet nodeIds;
	TLongSet wayIds;
	int nSimple = 0;
	int nComplex = 0;

	TLongObjectMap<OsmNode> additionalNodes = new TLongObjectHashMap<>();
	TLongObjectMap<OsmWay> additionalWays = new TLongObjectHashMap<>();

	public RelationQueryBag(OsmStreamOutput outRelations)
	{
		this.outRelations = outRelations;
		additionalNodes = new TLongObjectHashMap<>();
		additionalWays = new TLongObjectHashMap<>();
		nodeIds = new TLongHashSet();
		wayIds = new TLongHashSet();
	}

	public RelationQueryBag(OsmStreamOutput outRelations,
			TLongObjectMap<OsmNode> additionalNodes,
			TLongObjectMap<OsmWay> additionalWays, TLongSet nodeIds,
			TLongSet wayIds)
	{
		this.outRelations = outRelations;
		this.additionalNodes = additionalNodes;
		this.additionalWays = additionalWays;
		this.nodeIds = nodeIds;
		this.wayIds = wayIds;
	}

}

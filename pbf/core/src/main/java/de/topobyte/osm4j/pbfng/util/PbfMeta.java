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

package de.topobyte.osm4j.pbfng.util;

import java.util.HashSet;
import java.util.Set;

import crosby.binary.Osmformat;
import de.topobyte.osm4j.core.model.iface.EntityType;

public class PbfMeta
{

	public static boolean hasMixedContent(Osmformat.PrimitiveBlock block)
	{
		int count = block.getPrimitivegroupCount();
		if (count <= 1) {
			return false;
		}
		Set<EntityType> types = getContentTypes(block);
		return types.size() > 1;
	}

	public static Set<EntityType> getContentTypes(Osmformat.PrimitiveBlock block)
	{
		int count = block.getPrimitivegroupCount();
		Set<EntityType> types = new HashSet<>();
		for (int i = 0; i < count; i++) {
			Osmformat.PrimitiveGroup group = block.getPrimitivegroup(i);
			EntityType type = getEntityType(group);
			if (type != null) {
				types.add(type);
			}
		}
		return types;
	}

	public static EntityType getEntityType(Osmformat.PrimitiveGroup group)
	{
		if (group.hasDense() || group.getNodesCount() > 0) {
			return EntityType.Node;
		}
		if (group.getWaysCount() > 0) {
			return EntityType.Way;
		}
		if (group.getRelationsCount() > 0) {
			return EntityType.Relation;
		}
		// Empty block!
		return null;
	}

}

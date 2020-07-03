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

package de.topobyte.osm4j.tbo.writerhelper;

import de.topobyte.osm4j.core.model.iface.EntityType;

public class EntityTypeHelper
{

	public static EntityType getType(int typeByte)
	{
		switch (typeByte) {
		default:
		case 0x1:
			return EntityType.Node;
		case 0x2:
			return EntityType.Way;
		case 0x4:
			return EntityType.Relation;
		}
	}

	public static int getByte(EntityType type)
	{
		if (type == EntityType.Node) {
			return 0x1;
		} else if (type == EntityType.Way) {
			return 0x2;
		}
		return 0x4;
	}

}

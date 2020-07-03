package de.topobyte.osm4j.extra.entitywriter;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;

public class EntityWriters
{

	public static EntityWriter create(EntityType type, OsmOutputStream output)
	{
		switch (type) {
		default:
			return null;
		case Node:
			return new NodeWriter(output);
		case Way:
			return new WayWriter(output);
		case Relation:
			return new RelationWriter(output);
		}
	}

}

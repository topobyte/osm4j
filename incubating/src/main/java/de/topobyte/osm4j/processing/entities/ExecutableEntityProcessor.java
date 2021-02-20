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

import java.io.IOException;
import java.nio.file.Path;

import org.locationtech.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecordWithTags;
import de.topobyte.osm4j.geometry.OsmEntityGeometryHandler;
import de.topobyte.osm4j.processing.entities.filter.DefaultEntityFilter;
import de.topobyte.osm4j.processing.entities.filter.EntityFilter;
import de.topobyte.osm4j.utils.OsmFileInput;

public class ExecutableEntityProcessor extends EntityProcessor
{

	private EntityFilter filter;

	public ExecutableEntityProcessor(OsmEntityGeometryHandler handler,
			NodeDB nodeDB, VarDB<WayRecordWithTags> wayDB, Geometry boundary,
			Path failedPolygonsDir, EntityFilter filter)
	{
		super(handler, nodeDB, wayDB, boundary, failedPolygonsDir);
		if (filter != null) {
			this.filter = filter;
		} else {
			this.filter = new DefaultEntityFilter();
		}
	}

	public void execute(OsmFileInput nodesFile, OsmFileInput waysFile,
			OsmFileInput relationsFile) throws IOException
	{
		OsmIteratorInput input;
		OsmIterator iterator;

		input = relationsFile.createIterator(true, false);
		iterator = input.getIterator();
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Relation) {
				OsmRelation relation = (Relation) container.getEntity();
				if (!filter.filterRelation(relation)) {
					continue;
				}
				processRelationPass1(relation);
			}
		}
		input.close();

		relationsPass1Done();

		input = nodesFile.createIterator(true, false);
		iterator = input.getIterator();
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				OsmNode node = (Node) container.getEntity();
				if (!filter.filterNode(node)) {
					continue;
				}
				processNode(node);
			}
		}
		input.close();

		input = waysFile.createIterator(true, false);
		iterator = input.getIterator();
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Way) {
				OsmWay way = (Way) container.getEntity();
				if (!filter.filterWay(way)) {
					continue;
				}
				processWay(way);
			}
		}
		input.close();

		input = relationsFile.createIterator(true, false);
		iterator = input.getIterator();
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Relation) {
				OsmRelation relation = (Relation) container.getEntity();
				if (!filter.filterRelation(relation)) {
					continue;
				}
				processRelationPass2(relation);
			}
		}
		input.close();
	}

}

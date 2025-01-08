// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.pbf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;

import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.system.utils.SystemPaths;

public class TestReadHistoryFile
{

	@Test
	public void test() throws IOException
	{
		Path fileInput = SystemPaths.HOME
				.resolve("Downloads/berlin-internal.osh.pbf");

		InputStream input = Files.newInputStream(fileInput);

		time();
		PbfIterator iterator = new PbfIterator(input, true);
		Class<?> clazz = null;
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			OsmEntity entity = container.getEntity();
			if (clazz != entity.getClass()) {
				time();
				System.out.println(entity.getClass());
				clazz = entity.getClass();
			}
			if (container.getType() == EntityType.Node) {
				OsmNode node = (OsmNode) entity;
				if (Double.isNaN(node.getLatitude())) {
					System.out.println("yo: " + node.getId());
					System.exit(1);
				}
			}
		}
		time();
	}

	private void time()
	{
		System.out.println(LocalTime.now());
	}

}

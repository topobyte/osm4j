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

package de.topobyte.osm4j.changeset.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Test;

import de.topobyte.osm4j.Util;
import de.topobyte.osm4j.changeset.OsmChangeset;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsHandler;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsReader;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.OsmTag;

public class TestRead implements OsmChangesetsHandler
{

	@Test
	public void test() throws IOException, OsmInputException
	{
		String filename = "003-338-100.osm.gz";

		InputStream cinput = Util.stream(filename);
		InputStream input = new GzipCompressorInputStream(cinput);

		OsmChangesetsReader reader = new OsmChangesetsReader(input);
		reader.setHandler(this);
		reader.read();
	}

	@Override
	public void handle(OsmChangeset changeset) throws IOException
	{
		System.out.println(String.format(
				"changeset %d, created at: %s, open? %b, closed at: %s, num changes: %d",
				changeset.getId(), changeset.getCreatedAt(), changeset.isOpen(),
				changeset.getClosedAt(), changeset.getNumChanges()));
		for (OsmTag tag : changeset.getTags()) {
			System.out.println(
					String.format("  %s=%s", tag.getKey(), tag.getValue()));
		}
	}

	@Override
	public void complete() throws IOException
	{
		System.out.println("complete");
	}

}

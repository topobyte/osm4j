// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.osc.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.osc.OsmChange;
import de.topobyte.osm4j.osc.dynsax.OsmChangeHandler;
import de.topobyte.osm4j.osc.dynsax.OsmOscReader;

public class TestBbox implements OsmChangeHandler
{

	// Area around Berlin
	private BBox bbox = new BBox(13.032531, 52.698857, 13.754882, 52.344568);
	private Envelope envelope = bbox.toEnvelope();

	@Test
	public void test() throws IOException, OsmInputException
	{
		String filename = "003-338-100.osc.gz";

		InputStream cinput = Util.stream(filename);
		InputStream input = new GzipCompressorInputStream(cinput);

		OsmOscReader reader = new OsmOscReader(input, true);
		reader.setHandler(this);
		reader.read();
	}

	@Override
	public void handle(OsmChange change) throws IOException
	{
		InMemoryListDataSet data = change.getElements();
		System.out.println(
				String.format("change: %s, %d nodes, %d ways, %d relations",
						change.getType(), data.getNodes().size(),
						data.getWays().size(), data.getRelations().size()));

		for (OsmNode node : data.getNodes()) {
			if (envelope.contains(node.getLongitude(), node.getLatitude())) {
				Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);
				System.out.println(tags);
			}
		}
	}

	@Override
	public void complete() throws IOException
	{
		System.out.println("complete");
	}

}

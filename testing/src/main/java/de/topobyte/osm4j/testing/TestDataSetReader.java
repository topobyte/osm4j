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

package de.topobyte.osm4j.testing;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestWay;

public class TestDataSetReader implements OsmReader
{

	private TestDataSet data;
	private OsmHandler handler;

	public TestDataSetReader(TestDataSet data)
	{
		this.data = data;
	}

	@Override
	public void setHandler(OsmHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void read() throws OsmInputException
	{
		try {
			if (data.hasBounds()) {
				handler.handle(data.getBounds());
			}
			for (TestNode node : data.getNodes()) {
				handler.handle(node);
			}
			for (TestWay way : data.getWays()) {
				handler.handle(way);
			}
			for (TestRelation relation : data.getRelations()) {
				handler.handle(relation);
			}
			handler.complete();
		} catch (IOException e) {
			throw new OsmInputException(e);
		}
	}

}

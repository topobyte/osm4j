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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputReader;

public class OsmCountCallback extends AbstractTaskSingleInputReader
{

	@Override
	protected String getHelpMessage()
	{
		return OsmCountCallback.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmCountCallback task = new OsmCountCallback();

		task.setup(args);

		task.readMetadata = true;

		task.init();

		try {
			task.run();
		} catch (OsmInputException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	private long nc = 0, wc = 0, rc = 0;
	private long closedWays = 0;

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		// ignore bounds
	}

	@Override
	public void handle(OsmNode node) throws IOException
	{
		nc++;
	}

	@Override
	public void handle(OsmWay way) throws IOException
	{
		wc++;
		boolean closed = way.getNodeId(0) == way.getNodeId(way
				.getNumberOfNodes() - 1);
		if (closed) {
			closedWays++;
		}
	}

	@Override
	public void handle(OsmRelation relation) throws IOException
	{
		rc++;
	}

	@Override
	public void complete() throws IOException
	{
		System.out.println("nodes:         " + nc);
		System.out.println("ways:          " + wc);
		System.out.println("ways (closed): " + closedWays);
		System.out.println("relations:     " + rc);
	}

}

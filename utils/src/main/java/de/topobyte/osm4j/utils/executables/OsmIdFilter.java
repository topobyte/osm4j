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

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputStreamSingleOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmIdFilter extends AbstractTaskSingleInputStreamSingleOutput
		implements OsmHandler
{

	private static final String OPTION_ID = "id";

	@Override
	protected String getHelpMessage()
	{
		return OsmIdFilter.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmIdFilter task = new OsmIdFilter();

		task.setup(args);

		task.readMetadata = true;
		task.writeMetadata = true;

		task.init();

		OsmReader reader = task.createReader();
		reader.setHandler(task);
		try {
			reader.read();
		} catch (OsmInputException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	private long id = 0;

	public OsmIdFilter()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_ID, true, true, "the id of elements to pass");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		String value = line.getOptionValue(OPTION_ID);
		try {
			id = Long.parseLong(value);
		} catch (NumberFormatException e) {
			System.out.println("unable to parse id value: '" + value + "'");
			System.exit(1);
		}
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		// ignore bounds
	}

	@Override
	public void handle(OsmNode node) throws IOException
	{
		if (take(node)) {
			osmOutputStream.write(node);
		}
	}

	@Override
	public void handle(OsmWay way) throws IOException
	{
		if (take(way)) {
			osmOutputStream.write(way);
		}
	}

	@Override
	public void handle(OsmRelation relation) throws IOException
	{
		if (take(relation)) {
			osmOutputStream.write(relation);
		}
	}

	private boolean take(OsmEntity entity)
	{
		return entity.getId() == id;
	}

	@Override
	public void complete() throws IOException
	{
		osmOutputStream.complete();
	}

}

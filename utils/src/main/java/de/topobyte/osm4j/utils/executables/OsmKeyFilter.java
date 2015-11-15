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
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamSingleOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmKeyFilter extends AbstractExecutableSingleInputStreamSingleOutput
		implements OsmHandler
{

	private static final String OPTION_KEY = "key";

	@Override
	protected String getHelpMessage()
	{
		return OsmKeyFilter.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmKeyFilter task = new OsmKeyFilter();

		task.setup(args);

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

	private String key;

	public OsmKeyFilter()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_KEY, true, true, "the key that elements have to carry");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		key = line.getOptionValue(OPTION_KEY);
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		osmOutputStream.write(bounds);
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

	@Override
	public void complete() throws IOException
	{
		osmOutputStream.complete();
	}

	private boolean take(OsmEntity entity)
	{
		Map<String, String> tags = OsmModelUtil.getTagsAsMap(entity);
		return tags.containsKey(key);
	}

}

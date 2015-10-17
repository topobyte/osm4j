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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmTagFilter extends AbstractTaskSingleInputReaderSingleOutput
{

	private static final String OPTION_KEY = "key";
	private static final String OPTION_VALUE = "value";

	@Override
	protected String getHelpMessage()
	{
		return OsmTagFilter.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmTagFilter task = new OsmTagFilter();

		task.setup(args);

		task.readMetadata = true;
		task.writeMetadata = true;

		task.init();

		try {
			task.run();
		} catch (OsmInputException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	private String key;
	private String value;

	public OsmTagFilter()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_KEY, true, true, "the key that elements have to carry");
		OptionHelper.add(options, OPTION_VALUE, true, true, "the value of the tag");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		key = line.getOptionValue(OPTION_KEY);
		value = line.getOptionValue(OPTION_VALUE);
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
		if (!tags.containsKey(key)) {
			return false;
		}
		String v = tags.get(key);
		return v.equals(value);
	}

}

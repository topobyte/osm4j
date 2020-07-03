// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.executables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import de.topobyte.osm4j.extra.relations.RelationGroupUtil;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.parsing.BooleanOption;
import de.topobyte.utilities.apache.commons.cli.parsing.StringOption;

public class RelationGraphInfo extends AbstractExecutableSingleInputStream
{

	private static final String OPTION_UNDIRECTED = "undirected";
	private static final String OPTION_KEYS = "keys";

	@Override
	protected String getHelpMessage()
	{
		return RelationGraphInfo.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		RelationGraphInfo task = new RelationGraphInfo();

		task.setup(args);

		task.init();

		task.run();

		task.finish();
	}

	private boolean undirected = false;
	private List<String> keys = new ArrayList<>();

	public RelationGraphInfo()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_UNDIRECTED, false, false, "build an undirected graph");
		OptionHelper.addL(options, OPTION_KEYS, true, false, "comma separated list of tag-keys to display for each start relation");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		try {
			BooleanOption oUndirected = ArgumentHelper.getBoolean(line,
					OPTION_UNDIRECTED);
			if (oUndirected.hasValue()) {
				undirected = true;
			}
		} catch (ArgumentParseException e) {
			System.out.println("Error while parsing option '"
					+ OPTION_UNDIRECTED + "'");
			System.exit(1);
		}

		StringOption oTags = ArgumentHelper.getString(line, OPTION_KEYS);
		if (oTags.hasValue()) {
			String[] values = oTags.getValue().split(",");
			keys.addAll(Arrays.asList(values));
		}
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		// TODO: for a file with nodes and ways included this is bad because it
		// loads them into memory, too
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		RelationGraph graph = new RelationGraph(true, undirected);
		graph.build(data.getRelations().valueCollection());
		List<Group> groups = graph.buildGroups();

		System.out
				.println(String.format("Number of groups: %d", groups.size()));

		for (Group group : groups) {
			group.setNumMembers(RelationGroupUtil.groupSize(group, data));
		}

		for (Group group : groups) {
			System.out.println(String.format(
					"Start: %d, relations: %d, members: %d", group.getStart(),
					group.getNumRelations(), group.getNumMembers()));
			try {
				OsmRelation relation = data.getRelation(group.getStart());
				Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
				for (String key : keys) {
					String value = tags.get(key);
					if (value == null) {
						continue;
					}
					System.out.println(String.format("%s=%s", key, value));
				}
			} catch (EntityNotFoundException e) {
				// continue
			}
		}
	}

}

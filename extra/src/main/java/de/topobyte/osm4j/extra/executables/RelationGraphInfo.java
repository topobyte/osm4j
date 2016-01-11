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
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;
import de.topobyte.utilities.apache.commons.cli.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.BooleanOption;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class RelationGraphInfo extends AbstractExecutableSingleInputStream
{

	private static final String OPTION_UNDIRECTED = "undirected";

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

	public RelationGraphInfo()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_UNDIRECTED, false, false, "build an undirected graph");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		BooleanOption option;
		try {
			option = ArgumentHelper.getBoolean(line, OPTION_UNDIRECTED);
			if (option.hasValue()) {
				undirected = true;
			}
		} catch (ArgumentParseException e) {
			System.out.println("Error while parsing option '"
					+ OPTION_UNDIRECTED + "'");
			System.exit(1);
		}
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();

		RelationGraph graph = new RelationGraph(true, undirected);
		graph.build(iterator);
		List<Group> groups = graph.buildGroups();

		System.out
				.println(String.format("Number of groups: %d", groups.size()));
		for (Group group : groups) {
			System.out.println(String.format(
					"Start: %d, relations: %d, members: %d", group.getStart(),
					group.getNumRelations(), group.getNumMembers()));
		}
	}

}

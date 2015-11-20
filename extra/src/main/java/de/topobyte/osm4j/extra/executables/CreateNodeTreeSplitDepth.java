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

package de.topobyte.osm4j.extra.executables;

import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreator;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeSplitDepth extends CreateNodeTreeBase
{

	private static final String OPTION_SPLIT_DEPTH = "split_depth";

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeSplitDepth.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeSplitDepth task = new CreateNodeTreeSplitDepth();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private int splitDepth;

	public CreateNodeTreeSplitDepth()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_SPLIT_DEPTH, true, true, "how often to split the root node");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argSplitDepth = line.getOptionValue(OPTION_SPLIT_DEPTH);

		splitDepth = Integer.parseInt(argSplitDepth);
		if (splitDepth < 0) {
			System.out.println("Please specify a positive split depth");
			System.exit(1);
		}
	}

	private void execute() throws IOException
	{
		OsmIterator iterator = createIterator();
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		NodeTreeCreator creator = new NodeTreeCreator(iterator,
				Paths.get(pathOutput), fileNames, outputConfig);

		creator.initNewTree();

		DataTree tree = creator.getTree();
		tree.getRoot().split(splitDepth);
		tree.print();

		creator.execute();
	}

}

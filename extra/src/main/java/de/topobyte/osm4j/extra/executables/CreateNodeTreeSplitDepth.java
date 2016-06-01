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
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreator;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.SimpleNodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.ThreadedNodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.output.ClosingDataTreeOutputFactory;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
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
		OptionHelper.addL(options, OPTION_SPLIT_DEPTH, true, true, "how often to split the root node");
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
		if (!iterator.hasBounds()) {
			throw new IOException("Input does not provide bounds");
		}

		OsmBounds bounds = iterator.getBounds();
		System.out.println("bounds: " + bounds);

		Path pathTree = Paths.get(pathOutput);

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		DataTree tree = DataTreeUtil.initNewTree(pathTree, bounds);

		tree.getRoot().split(splitDepth);
		tree.print();

		DataTreeFiles treeFiles = new DataTreeFiles(pathTree, fileNames);
		DataTreeOutputFactory dataTreeOutputFactory = new ClosingDataTreeOutputFactory(
				treeFiles, outputConfig);

		boolean threaded = true;

		NodeTreeDistributorFactory distributorFactory;
		if (!threaded) {
			distributorFactory = new SimpleNodeTreeDistributorFactory();
		} else {
			distributorFactory = new ThreadedNodeTreeDistributorFactory();
		}

		NodeTreeCreator creator = new NodeTreeCreator(tree, iterator,
				dataTreeOutputFactory, distributorFactory);

		creator.execute();
	}

}

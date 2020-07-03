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
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeUtil;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreatorMaxNodes;
import de.topobyte.osm4j.extra.datatree.nodetree.count.NodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.count.SimpleNodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.count.ThreadedNodeTreeLeafCounterFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.SimpleNodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.ThreadedNodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.output.ClosingDataTreeOutputFactory;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeMaxNodes extends
		AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES = "filenames";
	private static final String OPTION_MAX_NODES = "max-nodes";

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeMaxNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeMaxNodes task = new CreateNodeTreeMaxNodes();

		task.setup(args);

		task.execute();
	}

	private int maxNodes;
	private String pathOutput;
	private String fileNames;

	public CreateNodeTreeMaxNodes()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
		OptionHelper.addL(options, OPTION_FILE_NAMES, true, true, "names of the data files to create");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argMaxNodes = line.getOptionValue(OPTION_MAX_NODES);

		maxNodes = Integer.parseInt(argMaxNodes);
		if (maxNodes < 1) {
			System.out.println("Please specify a max nodes >= 1");
			System.exit(1);
		}

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
		fileNames = line.getOptionValue(OPTION_FILE_NAMES);
	}

	private void execute() throws IOException
	{
		OsmIteratorInput input = getOsmFileInput().createIterator(false, false);
		OsmIterator iterator = input.getIterator();

		if (!iterator.hasBounds()) {
			System.out.println("Input does not provide bounds");
			System.exit(1);
		}

		OsmBounds bounds = iterator.getBounds();
		System.out.println("bounds: " + bounds);

		input.close();

		Path pathTree = Paths.get(pathOutput);

		DataTree tree = DataTreeUtil.initNewTree(pathTree, bounds);

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		DataTreeFiles treeFiles = new DataTreeFiles(pathTree, fileNames);
		DataTreeOutputFactory dataTreeOutputFactory = new ClosingDataTreeOutputFactory(
				treeFiles, outputConfig);

		boolean threaded = true;

		NodeTreeLeafCounterFactory counterFactory;
		NodeTreeDistributorFactory distributorFactory;

		if (!threaded) {
			counterFactory = new SimpleNodeTreeLeafCounterFactory();
			distributorFactory = new SimpleNodeTreeDistributorFactory();
		} else {
			counterFactory = new ThreadedNodeTreeLeafCounterFactory();
			distributorFactory = new ThreadedNodeTreeDistributorFactory();
		}

		NodeTreeCreatorMaxNodes creator = new NodeTreeCreatorMaxNodes(tree,
				getOsmFileInput(), dataTreeOutputFactory, maxNodes,
				SPLIT_INITIAL, SPLIT_ITERATION, pathTree, fileNames,
				outputConfig, counterFactory, distributorFactory);

		creator.buildTree();
	}

}

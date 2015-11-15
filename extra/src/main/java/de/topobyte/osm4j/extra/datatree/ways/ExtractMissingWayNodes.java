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

package de.topobyte.osm4j.extra.datatree.ways;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idextract.ExtractionItem;
import de.topobyte.osm4j.extra.idextract.Extractor;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractMissingWayNodes extends
		AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_IDS = "ids";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return ExtractMissingWayNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractMissingWayNodes task = new ExtractMissingWayNodes();

		task.setup(args);

		task.init();

		task.prepare();

		task.execute();
	}

	private String pathIdTree;
	private String pathOutputTree;

	private String fileNamesIds;
	private String fileNamesOutput;

	public ExtractMissingWayNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_FILE_NAMES_IDS, true, true, "names of the node id files in the tree");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String pathTree = line.getOptionValue(OPTION_TREE);
		pathIdTree = pathTree;
		pathOutputTree = pathTree;

		fileNamesIds = line.getOptionValue(OPTION_FILE_NAMES_IDS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);
	}

	private List<ExtractionItem> extractionItems = new ArrayList<>();

	public void prepare() throws IOException
	{
		DataTree tree = DataTreeOpener.open(new File(pathIdTree));

		File dirIdTree = new File(pathIdTree);
		File dirOutputTree = new File(pathOutputTree);

		DataTreeFiles filesIds = new DataTreeFiles(dirIdTree, fileNamesIds);
		DataTreeFiles filesOutput = new DataTreeFiles(dirOutputTree,
				fileNamesOutput);

		for (Node leaf : tree.getLeafs()) {
			File fileIds = filesIds.getFile(leaf);
			File fileOutput = filesOutput.getFile(leaf);
			ExtractionItem item = new ExtractionItem(fileIds.toPath(),
					fileOutput.toPath());
			extractionItems.add(item);
		}

	}

	public void execute() throws IOException
	{
		Extractor extractor = new Extractor(EntityType.Node, extractionItems,
				outputFormat, pbfConfig, tboConfig, writeMetadata);

		OsmIterator iterator = createIterator();
		extractor.execute(iterator);
		finish();
	}

}

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

import java.io.IOException;
import java.nio.file.Path;
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
import de.topobyte.osm4j.extra.idextract.Extractors;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class MissingWayNodesExtractor
{

	private OsmIterator iterator;

	private Path pathIdTree;
	private Path pathOutputTree;

	private String fileNamesIds;
	private String fileNamesOutput;

	private OsmOutputConfig outputConfig;

	private boolean threaded;

	public MissingWayNodesExtractor(OsmIterator iterator, Path pathIdTree,
			String fileNamesIds, Path pathOutputTree, String fileNamesOutput,
			OsmOutputConfig outputConfig, boolean threaded)
	{
		this.iterator = iterator;
		this.pathIdTree = pathIdTree;
		this.fileNamesIds = fileNamesIds;
		this.pathOutputTree = pathOutputTree;
		this.fileNamesOutput = fileNamesOutput;
		this.outputConfig = outputConfig;
		this.threaded = threaded;
	}

	public void execute() throws IOException
	{
		prepare();

		run();
	}

	private List<ExtractionItem> extractionItems = new ArrayList<>();

	private void prepare() throws IOException
	{
		DataTree tree = DataTreeOpener.open(pathIdTree);

		DataTreeFiles filesIds = new DataTreeFiles(pathIdTree, fileNamesIds);
		DataTreeFiles filesOutput = new DataTreeFiles(pathOutputTree,
				fileNamesOutput);

		for (Node leaf : tree.getLeafs()) {
			Path fileIds = filesIds.getPath(leaf);
			Path fileOutput = filesOutput.getPath(leaf);
			ExtractionItem item = new ExtractionItem(fileIds, fileOutput);
			extractionItems.add(item);
		}

	}

	private void run() throws IOException
	{
		Extractor extractor = Extractors.create(EntityType.Node,
				extractionItems, outputConfig, true, iterator, threaded);

		extractor.execute();
	}

}

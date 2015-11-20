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
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class MissingWayNodesExtractor
{

	private OsmIterator iterator;

	private Path pathIdTree;
	private Path pathOutputTree;

	private String fileNamesIds;
	private String fileNamesOutput;

	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata;

	public MissingWayNodesExtractor(OsmIterator iterator, Path pathIdTree,
			String fileNamesIds, Path pathOutputTree, String fileNamesOutput,
			FileFormat outputFormat, PbfConfig pbfConfig, TboConfig tboConfig,
			boolean writeMetadata)
	{
		this.iterator = iterator;
		this.pathIdTree = pathIdTree;
		this.fileNamesIds = fileNamesIds;
		this.pathOutputTree = pathOutputTree;
		this.fileNamesOutput = fileNamesOutput;

		this.outputFormat = outputFormat;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
		this.writeMetadata = writeMetadata;
	}

	public void execute() throws IOException
	{
		prepare();

		run();
	}

	private List<ExtractionItem> extractionItems = new ArrayList<>();

	private void prepare() throws IOException
	{
		DataTree tree = DataTreeOpener.open(pathIdTree.toFile());

		DataTreeFiles filesIds = new DataTreeFiles(pathIdTree, fileNamesIds);
		DataTreeFiles filesOutput = new DataTreeFiles(pathOutputTree,
				fileNamesOutput);

		for (Node leaf : tree.getLeafs()) {
			File fileIds = filesIds.getFile(leaf);
			File fileOutput = filesOutput.getFile(leaf);
			ExtractionItem item = new ExtractionItem(fileIds.toPath(),
					fileOutput.toPath());
			extractionItems.add(item);
		}

	}

	private void run() throws IOException
	{
		Extractor extractor = new Extractor(EntityType.Node, extractionItems,
				outputFormat, pbfConfig, tboConfig, writeMetadata);

		extractor.execute(iterator);
	}

}

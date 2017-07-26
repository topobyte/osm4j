// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.datatree.sort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.sort.MemorySort;

public class TreeFileSorter
{

	private Path pathTree;

	private String fileNamesUnsorted;
	private String fileNamesSorted;

	private FileFormat inputFormat;

	private OsmOutputConfig outputConfig;

	private boolean keepUnsorted;

	public TreeFileSorter(Path pathTree, String fileNamesUnsorted,
			String fileNamesSorted, FileFormat inputFormat,
			OsmOutputConfig outputConfig, boolean keepUnsorted)
	{
		this.pathTree = pathTree;
		this.fileNamesSorted = fileNamesSorted;
		this.fileNamesUnsorted = fileNamesUnsorted;
		this.inputFormat = inputFormat;
		this.outputConfig = outputConfig;
		this.keepUnsorted = keepUnsorted;
	}

	public void execute() throws IOException
	{
		prepare();

		sort();
	}

	private DataTree tree;
	private List<Node> leafs;

	protected void prepare() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());
		leafs = tree.getLeafs();
	}

	protected void sort() throws IOException
	{
		DataTreeFiles filesUnsorted = new DataTreeFiles(pathTree,
				fileNamesUnsorted);
		DataTreeFiles filesSorted = new DataTreeFiles(pathTree,
				fileNamesSorted);

		for (Node leaf : leafs) {
			Path unsorted = filesUnsorted.getPath(leaf);
			Path sorted = filesSorted.getPath(leaf);

			InputStream input = StreamUtil.bufferedInputStream(unsorted);
			OutputStream output = StreamUtil.bufferedOutputStream(sorted);

			OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
					inputFormat, outputConfig.isWriteMetadata());
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputConfig);
			MemorySort sort = new MemorySort(osmOutput, osmInput);
			sort.setIgnoreDuplicates(true);
			sort.run();

			output.close();
			input.close();

			if (!keepUnsorted) {
				Files.delete(unsorted);
			}
		}
	}

}

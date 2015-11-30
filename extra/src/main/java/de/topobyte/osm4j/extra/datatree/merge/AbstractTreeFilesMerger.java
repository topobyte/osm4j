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

package de.topobyte.osm4j.extra.datatree.merge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.merge.sorted.SortedMerge;
import de.topobyte.osm4j.utils.sort.MemorySortIterator;

public abstract class AbstractTreeFilesMerger implements TreeFilesMerger
{

	private Path pathTree;

	private List<String> fileNamesSorted = new ArrayList<>();
	private List<String> fileNamesUnsorted = new ArrayList<>();
	private String fileNamesOutput;

	private FileFormat inputFormat;
	private OsmOutputConfig outputConfig;

	private boolean deleteInput;

	public AbstractTreeFilesMerger(Path pathTree, List<String> fileNamesSorted,
			List<String> fileNamesUnsorted, String fileNamesOutput,
			FileFormat inputFormat, OsmOutputConfig outputConfig,
			boolean deleteInput)
	{
		this.pathTree = pathTree;
		this.fileNamesSorted = fileNamesSorted;
		this.fileNamesUnsorted = fileNamesUnsorted;
		this.fileNamesOutput = fileNamesOutput;
		this.inputFormat = inputFormat;
		this.outputConfig = outputConfig;
		this.deleteInput = deleteInput;
	}

	protected DataTree tree;
	protected List<Node> leafs;

	private long start = System.currentTimeMillis();

	protected void prepare() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());
		leafs = tree.getLeafs();
	}

	protected void mergeFiles(Node leaf) throws IOException
	{
		DataTreeFiles filesOutputNodes = new DataTreeFiles(pathTree,
				fileNamesOutput);

		List<File> inputFiles = new ArrayList<>();
		List<InputStream> inputs = new ArrayList<>();
		List<OsmIterator> osmInputs = new ArrayList<>();

		for (String fileName : fileNamesSorted) {
			DataTreeFiles files = new DataTreeFiles(pathTree, fileName);
			File file = files.getFile(leaf);
			inputFiles.add(file);

			InputStream input = StreamUtil.bufferedInputStream(file);
			inputs.add(input);

			OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
					inputFormat, outputConfig.isWriteMetadata());
			osmInputs.add(osmInput);
		}

		for (String fileName : fileNamesUnsorted) {
			DataTreeFiles files = new DataTreeFiles(pathTree, fileName);
			File file = files.getFile(leaf);
			inputFiles.add(file);

			InputStream input = StreamUtil.bufferedInputStream(file);
			inputs.add(input);

			OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
					inputFormat, outputConfig.isWriteMetadata());
			OsmIterator sorted = new MemorySortIterator(osmInput);
			osmInputs.add(sorted);
		}

		File fileOutputNodes = filesOutputNodes.getFile(leaf);

		OutputStream output = StreamUtil.bufferedOutputStream(fileOutputNodes);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputConfig);

		SortedMerge merge = new SortedMerge(osmOutput, osmInputs);
		merge.run();

		for (InputStream input : inputs) {
			input.close();
		}
		output.close();

		if (deleteInput) {
			for (File file : inputFiles) {
				file.delete();
			}
		}
	}

	protected void stats(int leafsDone)
	{
		long now = System.currentTimeMillis();
		long past = now - start;
		long estimate = Math.round((past / (double) leafsDone) * leafs.size());
		System.out.println(String.format("Past: %.2f", past / 1000 / 60.));
		System.out.println(String.format("Estimate: %.2f",
				estimate / 1000 / 60.));
	}

}

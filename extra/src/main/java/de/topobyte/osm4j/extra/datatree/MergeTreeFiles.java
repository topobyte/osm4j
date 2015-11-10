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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.utils.AbstractTask;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.osm4j.utils.merge.sorted.SortedMerge;
import de.topobyte.osm4j.utils.sort.MemorySortIterator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class MergeTreeFiles extends AbstractTask
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_INPUT_FORMAT = "input_format";
	private static final String OPTION_FILE_NAMES_SORTED = "input_sorted";
	private static final String OPTION_FILE_NAMES_UNSORTED = "input_unsorted";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";
	private static final String OPTION_DELETE = "delete";

	@Override
	protected String getHelpMessage()
	{
		return MergeTreeFiles.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		MergeTreeFiles task = new MergeTreeFiles();

		task.setup(args);

		task.prepare();

		task.execute();
	}

	private String pathTree;

	private List<String> fileNamesSorted = new ArrayList<>();
	private List<String> fileNamesUnsorted = new ArrayList<>();
	private String fileNamesOutput;

	private FileFormat inputFormat;
	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata = true;
	private boolean deleteInput;

	public MergeTreeFiles()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_SORTED, true, false, "name of a data file with sorted data in the tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_UNSORTED, true, false, "name of a data file with unsorted data in the tree");
		OptionHelper.add(options, OPTION_INPUT_FORMAT, true, true, "the file format of the input");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "name of files for merged data");
		OptionHelper.add(options, OPTION_DELETE, false, false, "delete input files");
		PbfOptions.add(options);
		TboOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String inputFormatName = line.getOptionValue(OPTION_INPUT_FORMAT);
		inputFormat = FileFormat.parseFileFormat(inputFormatName);
		if (inputFormat == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		String outputFormatName = line.getOptionValue(OPTION_OUTPUT_FORMAT);
		outputFormat = FileFormat.parseFileFormat(outputFormatName);
		if (outputFormat == null) {
			System.out.println("invalid output format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pbfConfig = PbfOptions.parse(line);
		tboConfig = TboOptions.parse(line);

		pathTree = line.getOptionValue(OPTION_TREE);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);

		String[] fileNamesSorted = line
				.getOptionValues(OPTION_FILE_NAMES_SORTED);
		String[] fileNamesUnsorted = line
				.getOptionValues(OPTION_FILE_NAMES_UNSORTED);

		if (fileNamesSorted != null) {
			this.fileNamesSorted.addAll(Arrays.asList(fileNamesSorted));
		}
		if (fileNamesUnsorted != null) {
			this.fileNamesUnsorted.addAll(Arrays.asList(fileNamesUnsorted));
		}

		int numFiles = this.fileNamesSorted.size()
				+ this.fileNamesUnsorted.size();
		if (numFiles < 2) {
			System.out.println("not enough input files");
			System.out.println("please specify at least two files to merge");
			System.exit(1);
		}

		deleteInput = line.hasOption(OPTION_DELETE);
	}

	private DataTree tree;
	private File dirTree;
	private List<Node> leafs;

	private long start = System.currentTimeMillis();

	public void prepare() throws IOException
	{
		tree = DataTreeOpener.open(new File(pathTree));
		dirTree = new File(pathTree);
		leafs = tree.getLeafs();
	}

	public void execute() throws IOException
	{
		DataTreeFiles filesOutputNodes = new DataTreeFiles(dirTree,
				fileNamesOutput);

		int i = 0;
		for (Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			List<File> inputFiles = new ArrayList<>();
			List<InputStream> inputs = new ArrayList<>();
			List<OsmIterator> osmInputs = new ArrayList<>();

			for (String fileName : fileNamesSorted) {
				DataTreeFiles files = new DataTreeFiles(dirTree, fileName);
				File file = files.getFile(leaf);
				inputFiles.add(file);

				InputStream input = StreamUtil.bufferedInputStream(file);
				inputs.add(input);

				OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
						inputFormat, writeMetadata);
				osmInputs.add(osmInput);
			}

			for (String fileName : fileNamesUnsorted) {
				DataTreeFiles files = new DataTreeFiles(dirTree, fileName);
				File file = files.getFile(leaf);
				inputFiles.add(file);

				InputStream input = StreamUtil.bufferedInputStream(file);
				inputs.add(input);

				OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
						inputFormat, writeMetadata);
				OsmIterator sorted = new MemorySortIterator(osmInput);
				osmInputs.add(sorted);
			}

			File fileOutputNodes = filesOutputNodes.getFile(leaf);

			OutputStream output = StreamUtil
					.bufferedOutputStream(fileOutputNodes);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputFormat, writeMetadata, pbfConfig, tboConfig);

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

			stats(i);
		}
	}

	private void stats(int leafsDone)
	{
		long now = System.currentTimeMillis();
		long past = now - start;
		long estimate = Math.round((past / (double) leafsDone) * leafs.size());
		System.out.println(String.format("Past: %.2f", past / 1000 / 60.));
		System.out.println(String.format("Estimate: %.2f",
				estimate / 1000 / 60.));
	}

}

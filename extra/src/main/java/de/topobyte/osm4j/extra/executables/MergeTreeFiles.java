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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.topobyte.osm4j.extra.datatree.merge.SimpleTreeFilesMerger;
import de.topobyte.osm4j.extra.datatree.merge.ThreadedTreeFilesMerger;
import de.topobyte.osm4j.extra.datatree.merge.TreeFilesMerger;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class MergeTreeFiles extends AbstractExecutableInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_SORTED = "input-sorted";
	private static final String OPTION_FILE_NAMES_UNSORTED = "input-unsorted";
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

		task.execute();
	}

	private String pathTree;

	private List<String> fileNamesSorted = new ArrayList<>();
	private List<String> fileNamesUnsorted = new ArrayList<>();
	private String fileNamesOutput;

	private boolean deleteInput;

	public MergeTreeFiles()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_FILE_NAMES_SORTED, true, false, "name of a data file with sorted data in the tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_UNSORTED, true, false, "name of a data file with unsorted data in the tree");
		OptionHelper.addL(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.addL(options, OPTION_FILE_NAMES_OUTPUT, true, true, "name of files for merged data");
		OptionHelper.addL(options, OPTION_DELETE, false, false, "delete input files");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

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

	public void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, deleteInput);

		boolean threaded = true;

		TreeFilesMerger merger;
		if (!threaded) {
			merger = new SimpleTreeFilesMerger(Paths.get(pathTree),
					fileNamesSorted, fileNamesUnsorted, fileNamesOutput,
					inputFormat, outputConfig, deleteInput);
		} else {
			merger = new ThreadedTreeFilesMerger(Paths.get(pathTree),
					fileNamesSorted, fileNamesUnsorted, fileNamesOutput,
					inputFormat, outputConfig, deleteInput);
		}

		merger.execute();
	}

}

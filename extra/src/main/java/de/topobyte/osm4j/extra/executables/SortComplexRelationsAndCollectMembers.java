// Copyright 2016 Sebastian Kuerten
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.topobyte.osm4j.extra.batch.BatchFilesUtil;
import de.topobyte.osm4j.extra.relations.ComplexRelationsSorterAndMemberCollector;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmFile;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmFileSetInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;

public class SortComplexRelationsAndCollectMembers extends
		AbstractExecutableSingleInputFileOutput
{

	private static final String OPTION_INPUT_BBOXES = "bboxes";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_OUTPUT_BBOXES = "output_bboxes";
	private static final String OPTION_INPUT_OLD = "input_old";
	private static final String OPTION_MAX_MEMBERS = "max_members";

	@Override
	protected String getHelpMessage()
	{
		return SortComplexRelationsAndCollectMembers.class.getSimpleName()
				+ " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SortComplexRelationsAndCollectMembers task = new SortComplexRelationsAndCollectMembers();

		task.setup(args);

		task.execute();
	}

	private Path pathInputBboxes;
	private Path pathOutput;
	private Path pathOutputBboxes;
	private Path pathInputOld;

	private String fileNamesRelations;

	private int maxMembers;

	public SortComplexRelationsAndCollectMembers()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT_BBOXES, true, true, "bbox information file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		OptionHelper.addL(options, OPTION_OUTPUT_BBOXES, true, true, "bbox information file");
		OptionHelper.addL(options, OPTION_INPUT_OLD, true, true, "input: relations (splitted)");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS, true, true, "maximum number of nodes per batch");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInputBboxes = Paths.get(line.getOptionValue(OPTION_INPUT_BBOXES));
		pathOutput = Paths.get(line.getOptionValue(OPTION_OUTPUT));
		pathOutputBboxes = Paths.get(line.getOptionValue(OPTION_OUTPUT_BBOXES));
		pathInputOld = Paths.get(line.getOptionValue(OPTION_INPUT_OLD));

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);

		try {
			maxMembers = ArgumentHelper.getInteger(line, OPTION_MAX_MEMBERS)
					.getValue();
		} catch (ArgumentParseException e) {
			System.out.println(String.format(
					"Error while parsing option '%s': %s", OPTION_MAX_MEMBERS,
					e.getMessage()));
			System.exit(1);
		}
	}

	private void execute() throws IOException
	{
		OsmFileInput fileInput = getOsmFileInput();

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		List<Path> nodePathsComplex = BatchFilesUtil.getPaths(pathInputOld,
				"nodes" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> nodeFiles = createOsmFiles(nodePathsComplex);

		List<Path> wayPathsComplex = BatchFilesUtil.getPaths(pathInputOld,
				"ways" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> wayFiles = createOsmFiles(wayPathsComplex);

		OsmFileSetInput inputNodes = new OsmFileSetInput(nodeFiles);
		OsmFileSetInput inputWays = new OsmFileSetInput(wayFiles);

		ComplexRelationsSorterAndMemberCollector sorter = new ComplexRelationsSorterAndMemberCollector(
				fileInput, pathInputBboxes, pathOutput, fileNamesRelations,
				inputWays, inputNodes, outputConfig, pathOutputBboxes,
				maxMembers);

		sorter.execute();
	}

	private Collection<OsmFile> createOsmFiles(List<Path> paths)
	{
		List<OsmFile> files = new ArrayList<>();
		for (Path path : paths) {
			files.add(new OsmFile(path, inputFormat));
		}
		return files;
	}

}

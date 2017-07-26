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

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.extra.extracts.ExtractionFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionFilesBuilder;
import de.topobyte.osm4j.extra.extracts.ExtractionFilesHelper;
import de.topobyte.osm4j.extra.extracts.FileNameDefaults;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;

public class BuildExtractionFiles extends AbstractExecutableInput
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_MAX_NODES = "max-nodes";
	private static final String OPTION_MAX_MEMBERS_SIMPLE = "max-members-simple";
	private static final String OPTION_MAX_MEMBERS_COMPLEX = "max-members-complex";
	private static final String OPTION_COMPUTE_BBOX = "compute-bbox";

	private static final String OPTION_KEEP_ALL = "keep-all";
	private static final String OPTION_KEEP_SPLITTED = "keep-splitted";
	private static final String OPTION_KEEP_SPLITTED_NODES = "keep-splitted-nodes";
	private static final String OPTION_KEEP_SPLITTED_WAYS = "keep-splitted-ways";
	private static final String OPTION_KEEP_SPLITTED_RELATIONS = "keep-splitted-relations";
	private static final String OPTION_KEEP_WAYS_BY_NODES = "keep-ways-by-nodes";
	private static final String OPTION_KEEP_RELATIONS = "keep-relations";
	private static final String OPTION_KEEP_RELATION_BATCHES = "keep-relation-batches";
	private static final String OPTION_KEEP_NONTREE_RELATIONS = "keep-nontree-relations";
	private static final String OPTION_KEEP_UNSORTED_RELATIONS = "keep-unsorted-relations";

	@Override
	protected String getHelpMessage()
	{
		return BuildExtractionFiles.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException, OsmInputException
	{
		BuildExtractionFiles task = new BuildExtractionFiles();

		task.setup(args);

		task.execute();
	}

	private String pathInput;
	private String pathOutput;
	private int maxNodes;
	private boolean includeMetadata = true;
	private int maxMembersSimple;
	private int maxMembersComplex;
	private boolean computeBbox = false;

	private FileFormat outputFormat = FileFormat.TBO;
	private ExtractionFileNames fileNames;

	private boolean keepAll = false;
	private boolean keepSplitted = false;
	private boolean keepSplittedNodes = false;
	private boolean keepSplittedWays = false;
	private boolean keepSplittedRelations = false;
	private boolean keepWaysByNodes = false;
	private boolean keepRelations = false;
	private boolean keepRelationBatches = false;
	private boolean keepNonTreeRelations = false;
	private boolean keepUnsortedRelations = false;

	public BuildExtractionFiles()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT, true, true, "input file");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.addL(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS_SIMPLE, true, true, "maximum number of nodes per batch");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS_COMPLEX, true, true, "maximum number of nodes per batch");
		OptionHelper.addL(options, OPTION_COMPUTE_BBOX, false, false, "compute bbox instead of using bbox declared in input file");
		ExtractionFilesHelper.addOptions(options);
		OptionHelper.addL(options, OPTION_KEEP_ALL, false, false, "keep all temporary files");
		OptionHelper.addL(options, OPTION_KEEP_SPLITTED, false, false, "keep the files containing only input nodes/ways/relations");
		OptionHelper.addL(options, OPTION_KEEP_SPLITTED_NODES, false, false, "keep the file containing only input nodes");
		OptionHelper.addL(options, OPTION_KEEP_SPLITTED_WAYS, false, false, "keep the file containing only input ways");
		OptionHelper.addL(options, OPTION_KEEP_SPLITTED_RELATIONS, false, false, "keep the file containing only input relations");
		OptionHelper.addL(options, OPTION_KEEP_WAYS_BY_NODES, false, false, "keep the directory with ways sorted by first node id");
		OptionHelper.addL(options, OPTION_KEEP_RELATIONS, false, false, "keep the files with simple and complex relations");
		OptionHelper.addL(options, OPTION_KEEP_RELATION_BATCHES, false, false, "keep the directories with relation batches");
		OptionHelper.addL(options, OPTION_KEEP_NONTREE_RELATIONS, false, false, "keep the files containing nontree relations");
		OptionHelper.addL(options, OPTION_KEEP_UNSORTED_RELATIONS, false, false, "keep the files containing unsorted complex relation groups");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInput = line.getOptionValue(OPTION_INPUT);
		pathOutput = line.getOptionValue(OPTION_OUTPUT);

		String argMaxNodes = line.getOptionValue(OPTION_MAX_NODES);

		maxNodes = Integer.parseInt(argMaxNodes);
		if (maxNodes < 1) {
			System.out.println("Please specify a max nodes >= 1");
			System.exit(1);
		}

		try {
			maxMembersSimple = ArgumentHelper
					.getInteger(line, OPTION_MAX_MEMBERS_SIMPLE).getValue();
		} catch (ArgumentParseException e) {
			System.out.println(
					String.format("Error while parsing option '%s': %s",
							OPTION_MAX_MEMBERS_SIMPLE, e.getMessage()));
			System.exit(1);
		}

		try {
			maxMembersComplex = ArgumentHelper
					.getInteger(line, OPTION_MAX_MEMBERS_COMPLEX).getValue();
		} catch (ArgumentParseException e) {
			System.out.println(
					String.format("Error while parsing option '%s': %s",
							OPTION_MAX_MEMBERS_COMPLEX, e.getMessage()));
			System.exit(1);
		}

		computeBbox = line.hasOption(OPTION_COMPUTE_BBOX);

		fileNames = FileNameDefaults.forFormat(outputFormat);

		ExtractionFilesHelper.parse(line, fileNames);

		keepAll = line.hasOption(OPTION_KEEP_ALL);
		keepSplitted = line.hasOption(OPTION_KEEP_SPLITTED);

		keepSplittedNodes = line.hasOption(OPTION_KEEP_SPLITTED_NODES);
		keepSplittedWays = line.hasOption(OPTION_KEEP_SPLITTED_WAYS);
		keepSplittedRelations = line.hasOption(OPTION_KEEP_SPLITTED_RELATIONS);

		keepWaysByNodes = line.hasOption(OPTION_KEEP_WAYS_BY_NODES);
		keepRelations = line.hasOption(OPTION_KEEP_RELATIONS);
		keepRelationBatches = line.hasOption(OPTION_KEEP_RELATION_BATCHES);
		keepNonTreeRelations = line.hasOption(OPTION_KEEP_NONTREE_RELATIONS);
		keepUnsortedRelations = line.hasOption(OPTION_KEEP_UNSORTED_RELATIONS);

		if (keepAll || keepSplitted) {
			keepSplittedNodes = true;
			keepSplittedWays = true;
			keepSplittedRelations = true;
		}
		if (keepAll) {
			keepWaysByNodes = true;
			keepRelations = true;
			keepRelationBatches = true;
			keepNonTreeRelations = true;
			keepUnsortedRelations = true;
		}
	}

	private void execute() throws IOException, OsmInputException
	{
		ExtractionFilesBuilder builder = new ExtractionFilesBuilder(
				Paths.get(pathInput), inputFormat, Paths.get(pathOutput),
				outputFormat, fileNames, maxNodes, includeMetadata,
				maxMembersSimple, maxMembersComplex, computeBbox);

		builder.setKeepSplittedNodes(keepSplittedNodes);
		builder.setKeepSplittedWays(keepSplittedWays);
		builder.setKeepSplittedRelations(keepSplittedRelations);
		builder.setKeepWaysByNodes(keepWaysByNodes);
		builder.setKeepRelations(keepRelations);
		builder.setKeepRelationBatches(keepRelationBatches);
		builder.setKeepNonTreeRelations(keepNonTreeRelations);
		builder.setKeepUnsortedRelations(keepUnsortedRelations);

		builder.execute();
	}

}

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
import de.topobyte.osm4j.extra.extracts.ExtractionFilesBuilder;
import de.topobyte.osm4j.utils.AbstractExecutableInput;
import de.topobyte.utilities.apache.commons.cli.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class BuildExtractionFiles extends AbstractExecutableInput
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_MAX_NODES = "max_nodes";
	private static final String OPTION_MAX_MEMBERS_SIMPLE = "max_members_simple";
	private static final String OPTION_MAX_MEMBERS_COMPLEX = "max_members_complex";

	@Override
	protected String getHelpMessage()
	{
		return BuildExtractionFiles.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException,
			OsmInputException
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

	public BuildExtractionFiles()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "input file");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
		OptionHelper.add(options, OPTION_MAX_MEMBERS_SIMPLE, true, true, "maximum number of nodes per batch");
		OptionHelper.add(options, OPTION_MAX_MEMBERS_COMPLEX, true, true, "maximum number of nodes per batch");
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
			maxMembersSimple = ArgumentHelper.getInteger(line,
					OPTION_MAX_MEMBERS_SIMPLE).getValue();
		} catch (ArgumentParseException e) {
			System.out.println(String.format(
					"Error while parsing option '%s': %s",
					OPTION_MAX_MEMBERS_SIMPLE, e.getMessage()));
			System.exit(1);
		}

		try {
			maxMembersComplex = ArgumentHelper.getInteger(line,
					OPTION_MAX_MEMBERS_COMPLEX).getValue();
		} catch (ArgumentParseException e) {
			System.out.println(String.format(
					"Error while parsing option '%s': %s",
					OPTION_MAX_MEMBERS_COMPLEX, e.getMessage()));
			System.exit(1);
		}
	}

	private void execute() throws IOException, OsmInputException
	{
		ExtractionFilesBuilder builder = new ExtractionFilesBuilder(
				Paths.get(pathInput), inputFormat, Paths.get(pathOutput),
				maxNodes, includeMetadata, maxMembersSimple, maxMembersComplex);

		builder.execute();
	}

}

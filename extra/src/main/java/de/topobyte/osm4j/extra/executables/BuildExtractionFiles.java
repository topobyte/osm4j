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

import de.topobyte.osm4j.extra.extracts.ExtractionFilesBuilder;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class BuildExtractionFiles extends AbstractExecutableInputOutput
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_MAX_NODES = "max_nodes";

	@Override
	protected String getHelpMessage()
	{
		return BuildExtractionFiles.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		BuildExtractionFiles task = new BuildExtractionFiles();

		task.setup(args);

		task.execute();
	}

	private String pathInput;
	private String pathOutput;
	private int maxNodes;

	public BuildExtractionFiles()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "input file");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");
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
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		ExtractionFilesBuilder builder = new ExtractionFilesBuilder(
				Paths.get(pathInput), inputFormat, Paths.get(pathOutput),
				outputConfig, maxNodes);

		builder.execute();
	}

}

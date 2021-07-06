// Copyright 2021 Sebastian Kuerten
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.extra.regions.DiverseRegionExtractor;
import de.topobyte.osm4j.extra.regions.DiverseRegionExtractor.Naming;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputFile;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.EnumArgument;

public class ExtractDiverseRegions extends AbstractExecutableSingleInputFile
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_NODE_INDEX = "nodes-index";
	private static final String OPTION_NODE_DATA = "nodes-data";
	private static final String OPTION_WAY_INDEX = "ways-index";
	private static final String OPTION_WAY_DATA = "ways-data";
	private static final String OPTION_NAMING = "naming";

	@Override
	protected String getHelpMessage()
	{
		return ExtractDiverseRegions.class.getSimpleName() + " [options]";
	}

	@Override
	protected String getFooter()
	{
		return "\nExtract diverse regions as *.smx from an input file such as administrative boundaries"
				+ " of various levels and postal codes.";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractDiverseRegions task = new ExtractDiverseRegions();

		task.setup(args);

		task.execute();
	}

	private String argOutput;
	private Path argNodeData;
	private Path argNodeIndex;
	private Path argWayData;
	private Path argWayIndex;
	private String argNaming;
	private Naming naming;

	public ExtractDiverseRegions()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_NODE_INDEX, true, true, "file", "node index");
		OptionHelper.addL(options, OPTION_NODE_DATA, true, true, "file", "node data");
		OptionHelper.addL(options, OPTION_WAY_INDEX, true, true, "file", "way index (no-tags)");
		OptionHelper.addL(options, OPTION_WAY_DATA, true, true, "file", "way data (no-tags)");
		OptionHelper.addL(options, OPTION_OUTPUT, true, true, "directory", "a directory to put the exported osm files to");
		OptionHelper.addL(options, OPTION_NAMING, true, false, "file naming scheme");
		// @formatter:on
	}

	@Override
	public void setup(String[] args)
	{
		super.setup(args);

		argOutput = line.getOptionValue(OPTION_OUTPUT);
		argNodeData = Paths.get(line.getOptionValue(OPTION_NODE_DATA));
		argNodeIndex = Paths.get(line.getOptionValue(OPTION_NODE_INDEX));
		argWayData = Paths.get(line.getOptionValue(OPTION_WAY_DATA));
		argWayIndex = Paths.get(line.getOptionValue(OPTION_WAY_INDEX));
		argNaming = line.getOptionValue(OPTION_NAMING);

		naming = Naming.WITH_NAME;
		if (argNaming != null) {
			EnumArgument<Naming> namingArgs = new EnumArgument<>(Naming.class);
			naming = namingArgs.parse(argNaming);
			if (naming == null) {
				System.out.println(String.format(
						"invalid argument for parameter '%s'", OPTION_NAMING));
				System.out.println("possible values: "
						+ namingArgs.getPossibleNames(false));
				System.exit(1);
			}
		}
	}

	public void execute()
	{
		DiverseRegionExtractor extractor = new DiverseRegionExtractor();
		extractor.prepare(getOsmFile(), argOutput, argNodeData, argNodeIndex,
				argWayData, argWayIndex, naming);
		try {
			extractor.execute();
		} catch (FileNotFoundException e) {
			System.out.println("file not found: " + pathInput);
			System.exit(1);
		} catch (IOException e) {
			System.out.println("error while extracting");
			e.printStackTrace();
			System.exit(1);
		}
	}

}

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
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.extra.relations.RelationsSplitterAndMemberCollector;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SplitRelationsAndCollectMembers extends
		AbstractExecutableInputOutput
{

	private static final String OPTION_INPUT_NODES = "input-nodes";
	private static final String OPTION_INPUT_WAYS = "input-ways";

	private static final String OPTION_INPUT_SIMPLE = "input-simple";
	private static final String OPTION_INPUT_COMPLEX = "input-complex";

	private static final String OPTION_OUTPUT_SIMPLE = "output-simple";
	private static final String OPTION_OUTPUT_COMPLEX = "output-complex";

	@Override
	protected String getHelpMessage()
	{
		return SplitRelationsAndCollectMembers.class.getSimpleName()
				+ " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitRelationsAndCollectMembers task = new SplitRelationsAndCollectMembers();

		task.setup(args);

		task.execute();
	}

	private String pathInputNodes;
	private String pathInputWays;
	private String pathInputSimple;
	private String pathInputComplex;
	private String pathOutputSimple;
	private String pathOutputComplex;

	public SplitRelationsAndCollectMembers()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT_NODES, true, true, "input: nodes");
		OptionHelper.addL(options, OPTION_INPUT_WAYS, true, true, "input: ways");
		OptionHelper.addL(options, OPTION_INPUT_SIMPLE, true, true, "input: simple relations");
		OptionHelper.addL(options, OPTION_INPUT_COMPLEX, true, true, "input: complex relations");
		OptionHelper.addL(options, OPTION_OUTPUT_SIMPLE, true, true, "output: simple relations");
		OptionHelper.addL(options, OPTION_OUTPUT_COMPLEX, true, true, "output: complex relations");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInputNodes = line.getOptionValue(OPTION_INPUT_NODES);
		pathInputWays = line.getOptionValue(OPTION_INPUT_WAYS);
		pathInputSimple = line.getOptionValue(OPTION_INPUT_SIMPLE);
		pathInputComplex = line.getOptionValue(OPTION_INPUT_COMPLEX);
		pathOutputSimple = line.getOptionValue(OPTION_OUTPUT_SIMPLE);
		pathOutputComplex = line.getOptionValue(OPTION_OUTPUT_COMPLEX);
	}

	private void execute() throws IOException
	{
		OsmFileInput inputNodes = new OsmFileInput(Paths.get(pathInputNodes),
				inputFormat);
		OsmFileInput inputWays = new OsmFileInput(Paths.get(pathInputWays),
				inputFormat);
		OsmFileInput inputSimpleRelations = new OsmFileInput(
				Paths.get(pathInputSimple), inputFormat);
		OsmFileInput inputComplexRelations = new OsmFileInput(
				Paths.get(pathInputComplex), inputFormat);

		Path pathOutputSimpleRelations = Paths.get(pathOutputSimple);
		Path pathOutputComplexRelations = Paths.get(pathOutputComplex);

		String fileNamesRelations = "relations"
				+ OsmIoUtils.extension(outputFormat);

		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		RelationsSplitterAndMemberCollector task = new RelationsSplitterAndMemberCollector(
				inputSimpleRelations, inputComplexRelations,
				pathOutputSimpleRelations, pathOutputComplexRelations,
				fileNamesRelations, inputWays, inputNodes, outputConfig);

		task.execute();
	}

}

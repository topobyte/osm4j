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

import de.topobyte.osm4j.extra.relations.NonTreeRelationsSplitter;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;

public class SplitNonTreeRelations extends AbstractExecutableInputOutput
{

	private static final String OPTION_INPUT_SIMPLE = "input-simple";
	private static final String OPTION_INPUT_COMPLEX = "input-complex";

	private static final String OPTION_INPUT_SIMPLE_BBOXES = "input-simple-bboxes";
	private static final String OPTION_INPUT_COMPLEX_BBOXES = "input-complex-bboxes";

	private static final String OPTION_INPUT_SIMPLE_OLD = "input-simple-old";
	private static final String OPTION_INPUT_COMPLEX_OLD = "input-complex-old";

	private static final String OPTION_OUTPUT_SIMPLE = "output-simple";
	private static final String OPTION_OUTPUT_COMPLEX = "output-complex";

	private static final String OPTION_OUTPUT_SIMPLE_BBOXES = "output-simple-bboxes";
	private static final String OPTION_OUTPUT_COMPLEX_BBOXES = "output-complex-bboxes";

	private static final String OPTION_MAX_MEMBERS_SIMPLE = "max-members-simple";
	private static final String OPTION_MAX_MEMBERS_COMPLEX = "max-members-complex";

	@Override
	protected String getHelpMessage()
	{
		return SplitNonTreeRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitNonTreeRelations task = new SplitNonTreeRelations();

		task.setup(args);

		task.execute();
	}

	private String pathInputSimple;
	private String pathInputComplex;
	private String pathInputSimpleBboxes;
	private String pathInputComplexBboxes;
	private String pathInputSimpleOld;
	private String pathInputComplexOld;
	private String pathOutputSimple;
	private String pathOutputComplex;
	private String pathOutputSimpleBboxes;
	private String pathOutputComplexBboxes;
	private int maxMembersSimple;
	private int maxMembersComplex;

	public SplitNonTreeRelations()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_INPUT_SIMPLE, true, true, "input: simple relations");
		OptionHelper.addL(options, OPTION_INPUT_COMPLEX, true, true, "input: complex relations");
		OptionHelper.addL(options, OPTION_INPUT_SIMPLE_BBOXES, true, true, "input: simple relation bboxes");
		OptionHelper.addL(options, OPTION_INPUT_COMPLEX_BBOXES, true, true, "input: complex relation bboxes");
		OptionHelper.addL(options, OPTION_INPUT_SIMPLE_OLD, true, true, "input: simple relation (splitted)");
		OptionHelper.addL(options, OPTION_INPUT_COMPLEX_OLD, true, true, "input: complex relation (splitted)");
		OptionHelper.addL(options, OPTION_INPUT_SIMPLE, true, true, "input: simple relations");
		OptionHelper.addL(options, OPTION_INPUT_COMPLEX, true, true, "input: complex relations");
		OptionHelper.addL(options, OPTION_OUTPUT_SIMPLE, true, true, "output: simple relations");
		OptionHelper.addL(options, OPTION_OUTPUT_COMPLEX, true, true, "output: complex relations");
		OptionHelper.addL(options, OPTION_OUTPUT_SIMPLE_BBOXES, true, true, "output: simple relations bboxes");
		OptionHelper.addL(options, OPTION_OUTPUT_COMPLEX_BBOXES, true, true, "output: complex relations bboxes");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS_SIMPLE, true, true, "maximum number of nodes per batch");
		OptionHelper.addL(options, OPTION_MAX_MEMBERS_COMPLEX, true, true, "maximum number of nodes per batch");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInputSimple = line.getOptionValue(OPTION_INPUT_SIMPLE);
		pathInputComplex = line.getOptionValue(OPTION_INPUT_COMPLEX);
		pathInputSimpleBboxes = line.getOptionValue(OPTION_INPUT_SIMPLE_BBOXES);
		pathInputComplexBboxes = line
				.getOptionValue(OPTION_INPUT_COMPLEX_BBOXES);
		pathInputSimpleOld = line.getOptionValue(OPTION_INPUT_SIMPLE_OLD);
		pathInputComplexOld = line.getOptionValue(OPTION_INPUT_COMPLEX_OLD);
		pathOutputSimple = line.getOptionValue(OPTION_OUTPUT_SIMPLE);
		pathOutputComplex = line.getOptionValue(OPTION_OUTPUT_COMPLEX);
		pathOutputSimpleBboxes = line
				.getOptionValue(OPTION_OUTPUT_SIMPLE_BBOXES);
		pathOutputComplexBboxes = line
				.getOptionValue(OPTION_OUTPUT_COMPLEX_BBOXES);

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
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		NonTreeRelationsSplitter splitter = new NonTreeRelationsSplitter(
				Paths.get(pathInputSimple), Paths.get(pathInputComplex),
				Paths.get(pathInputSimpleBboxes),
				Paths.get(pathInputComplexBboxes),
				Paths.get(pathInputSimpleOld), Paths.get(pathInputComplexOld),
				Paths.get(pathOutputSimple), Paths.get(pathOutputComplex),
				inputFormat, outputConfig, Paths.get(pathOutputSimpleBboxes),
				Paths.get(pathOutputComplexBboxes), maxMembersSimple,
				maxMembersComplex, false);
		splitter.execute();
	}

}

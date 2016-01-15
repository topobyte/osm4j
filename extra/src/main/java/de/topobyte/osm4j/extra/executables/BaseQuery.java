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

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.extracts.ExtractionFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionFilesHelper;
import de.topobyte.osm4j.extra.extracts.FileNameDefaults;
import de.topobyte.osm4j.extra.extracts.query.Query;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class BaseQuery extends AbstractExecutableInputOutput
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_TMP = "tmp";
	private static final String OPTION_KEEP_TMP = "keep_tmp";
	private static final String OPTION_FAST_RELATION_QUERIES = "fast_relation_queries";

	public BaseQuery()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "directory with extraction files");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_TMP, true, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_KEEP_TMP, false, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_FAST_RELATION_QUERIES, false, false, "include relations based on their bounding box");
		ExtractionFilesHelper.addOptions(options);
		// @formatter:on
	}

	private ExtractionFileNames fileNames;

	protected Path pathInput;
	protected Path pathOutput;
	protected Path pathTmp;

	protected Path pathTree;
	protected Path pathSimpleRelations;
	protected Path pathComplexRelations;
	protected Path pathSimpleRelationsBboxes;
	protected Path pathComplexRelationsBboxes;

	protected DataTree tree;

	protected Envelope queryEnvelope;
	protected PredicateEvaluator test;

	protected boolean keepTmp;

	protected boolean simpleRelationTests;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInput = Paths.get(line.getOptionValue(OPTION_INPUT));

		pathOutput = Paths.get(line.getOptionValue(OPTION_OUTPUT));
		if (line.hasOption(OPTION_TMP)) {
			pathTmp = Paths.get(line.getOptionValue(OPTION_TMP));
		} else {
			pathTmp = null;
		}

		fileNames = FileNameDefaults.forFormat(inputFormat);

		ExtractionFilesHelper.parse(line, fileNames);

		pathTree = pathInput.resolve(fileNames.getTree());
		pathSimpleRelations = pathInput.resolve(fileNames.getSimpleRelations());
		pathComplexRelations = pathInput.resolve(fileNames
				.getComplexRelations());
		pathSimpleRelationsBboxes = pathInput.resolve(fileNames
				.getSimpleRelationsBboxes());
		pathComplexRelationsBboxes = pathInput.resolve(fileNames
				.getComplexRelationsBboxes());

		keepTmp = line.hasOption(OPTION_KEEP_TMP);
		simpleRelationTests = line.hasOption(OPTION_FAST_RELATION_QUERIES);
	}

	protected void execute() throws IOException
	{
		OsmOutputConfig outputConfigIntermediate = new OsmOutputConfig(
				FileFormat.TBO, null, new TboConfig(), writeMetadata);
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		Query query = new Query(pathOutput, pathTmp, pathTree,
				pathSimpleRelations, pathComplexRelations,
				pathSimpleRelationsBboxes, pathComplexRelationsBboxes,
				fileNames.getTreeNodes(), fileNames.getTreeWays(),
				fileNames.getTreeSimpleRelations(),
				fileNames.getTreeComplexRelations(),
				fileNames.getRelationNodes(), fileNames.getRelationWays(),
				fileNames.getRelationRelations(), queryEnvelope, test,
				inputFormat, outputConfigIntermediate, outputConfig, keepTmp,
				simpleRelationTests);
		query.execute();
	}

}

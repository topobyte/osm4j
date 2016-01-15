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
	private static final String OPTION_TREE = "tree";
	private static final String OPTION_SIMPLE_RELATIONS = "simple_relations";
	private static final String OPTION_COMPLEX_RELATIONS = "complex_relations";
	private static final String OPTION_SIMPLE_RELATIONS_BBOXES = "simple_relations_bboxes";
	private static final String OPTION_COMPLEX_RELATIONS_BBOXES = "complex_relations_bboxes";
	private static final String OPTION_FILE_NAMES_TREE_NODES = "tree_nodes";
	private static final String OPTION_FILE_NAMES_TREE_WAYS = "tree_ways";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE = "tree_simple_relations";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX = "tree_complex_relations";
	private static final String OPTION_FILE_NAMES_RELATION_NODES = "relation_nodes";
	private static final String OPTION_FILE_NAMES_RELATION_WAYS = "relation_ways";
	private static final String OPTION_FILE_NAMES_RELATION_RELATIONS = "relation_relations";

	public BaseQuery()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "directory with extraction files");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_TMP, true, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_KEEP_TMP, false, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_FAST_RELATION_QUERIES, false, false, "include relations based on their bounding box");
		OptionHelper.add(options, OPTION_TREE, true, false, "path to the data tree");
		OptionHelper.add(options, OPTION_SIMPLE_RELATIONS, true, false, "path to simple relation batches");
		OptionHelper.add(options, OPTION_COMPLEX_RELATIONS, true, false, "path to complex relation batches");
		OptionHelper.add(options, OPTION_SIMPLE_RELATIONS_BBOXES, true, false, "path to simple relation batches bboxes");
		OptionHelper.add(options, OPTION_COMPLEX_RELATIONS_BBOXES, true, false, "path to complex relation batches bboxes");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_NODES, true, false, "name of node files in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_WAYS, true, false, "name of way files in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE, true, false, "name of simple relations in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX, true, false, "name of complex relations in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_NODES, true, false, "name of node files in relation batches");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_WAYS, true, false, "name of way files in relation batches");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_RELATIONS, true, false, "name of relation files in relation batches");
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

		if (line.hasOption(OPTION_TREE)) {
			fileNames.setTree(line.getOptionValue(OPTION_TREE));
		}
		if (line.hasOption(OPTION_SIMPLE_RELATIONS)) {
			fileNames.setSimpleRelations(line
					.getOptionValue(OPTION_SIMPLE_RELATIONS));
		}
		if (line.hasOption(OPTION_COMPLEX_RELATIONS)) {
			fileNames.setComplexRelations(line
					.getOptionValue(OPTION_COMPLEX_RELATIONS));
		}
		if (line.hasOption(OPTION_SIMPLE_RELATIONS_BBOXES)) {
			fileNames.setSimpleRelationsBboxes(line
					.getOptionValue(OPTION_SIMPLE_RELATIONS_BBOXES));
		}
		if (line.hasOption(OPTION_COMPLEX_RELATIONS_BBOXES)) {
			fileNames.setComplexRelationsBboxes(line
					.getOptionValue(OPTION_COMPLEX_RELATIONS_BBOXES));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_NODES)) {
			fileNames.setTreeNodes(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_NODES));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_WAYS)) {
			fileNames.setTreeWays(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_WAYS));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE)) {
			fileNames.setTreeSimpleRelations(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX)) {
			fileNames.setTreeComplexRelations(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_NODES)) {
			fileNames.setRelationNodes(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_NODES));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_WAYS)) {
			fileNames.setRelationWays(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_WAYS));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_RELATIONS)) {
			fileNames.setRelationRelations(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_RELATIONS));
		}

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

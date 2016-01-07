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
import de.topobyte.osm4j.extra.extracts.query.Query;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class BaseQuery extends AbstractExecutableInputOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_TMP = "tmp";
	private static final String OPTION_KEEP_TMP = "keep_tmp";
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
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_TMP, true, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_KEEP_TMP, false, false, "directory to store intermediate files");
		OptionHelper.add(options, OPTION_TREE, true, true, "path to the data tree");
		OptionHelper.add(options, OPTION_SIMPLE_RELATIONS, true, true, "path to simple relation batches");
		OptionHelper.add(options, OPTION_COMPLEX_RELATIONS, true, true, "path to complex relation batches");
		OptionHelper.add(options, OPTION_SIMPLE_RELATIONS_BBOXES, true, true, "path to simple relation batches bboxes");
		OptionHelper.add(options, OPTION_COMPLEX_RELATIONS_BBOXES, true, true, "path to complex relation batches bboxes");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_NODES, true, true, "name of node files in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_WAYS, true, true, "name of way files in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE, true, true, "name of simple relations in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX, true, true, "name of complex relations in tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_NODES, true, true, "name of node files in relation batches");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_WAYS, true, true, "name of way files in relation batches");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATION_RELATIONS, true, true, "name of relation files in relation batches");
		// @formatter:on
	}

	protected Path pathOutput;
	protected Path pathTmp;
	protected Path pathTree;
	protected Path pathSimpleRelations;
	protected Path pathComplexRelations;
	protected Path pathSimpleRelationsBboxes;
	protected Path pathComplexRelationsBboxes;
	protected DataTree tree;

	protected String fileNamesTreeNodes;
	protected String fileNamesTreeWays;
	protected String fileNamesTreeSimpleRelations;
	protected String fileNamesTreeComplexRelations;
	protected String fileNamesRelationNodes;
	protected String fileNamesRelationWays;
	protected String fileNamesRelationRelations;

	protected Envelope queryEnvelope;
	protected PredicateEvaluator test;

	protected boolean keepTmp;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = Paths.get(line.getOptionValue(OPTION_OUTPUT));
		if (line.hasOption(OPTION_TMP)) {
			pathTmp = Paths.get(line.getOptionValue(OPTION_TMP));
		} else {
			pathTmp = null;
		}
		pathTree = Paths.get(line.getOptionValue(OPTION_TREE));
		pathSimpleRelations = Paths.get(line
				.getOptionValue(OPTION_SIMPLE_RELATIONS));
		pathComplexRelations = Paths.get(line
				.getOptionValue(OPTION_COMPLEX_RELATIONS));
		pathSimpleRelationsBboxes = Paths.get(line
				.getOptionValue(OPTION_SIMPLE_RELATIONS_BBOXES));
		pathComplexRelationsBboxes = Paths.get(line
				.getOptionValue(OPTION_COMPLEX_RELATIONS_BBOXES));

		fileNamesTreeNodes = line.getOptionValue(OPTION_FILE_NAMES_TREE_NODES);
		fileNamesTreeWays = line.getOptionValue(OPTION_FILE_NAMES_TREE_WAYS);
		fileNamesTreeSimpleRelations = line
				.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE);
		fileNamesTreeComplexRelations = line
				.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX);

		fileNamesRelationNodes = line
				.getOptionValue(OPTION_FILE_NAMES_RELATION_NODES);
		fileNamesRelationWays = line
				.getOptionValue(OPTION_FILE_NAMES_RELATION_WAYS);
		fileNamesRelationRelations = line
				.getOptionValue(OPTION_FILE_NAMES_RELATION_RELATIONS);

		keepTmp = line.hasOption(OPTION_KEEP_TMP);
	}

	protected void execute() throws IOException
	{
		Query query = new Query(pathOutput, pathTmp, pathTree,
				pathSimpleRelations, pathComplexRelations,
				pathSimpleRelationsBboxes, pathComplexRelationsBboxes,
				fileNamesTreeNodes, fileNamesTreeWays,
				fileNamesTreeSimpleRelations, fileNamesTreeComplexRelations,
				fileNamesRelationNodes, fileNamesRelationWays,
				fileNamesRelationRelations, queryEnvelope, test, inputFormat,
				outputFormat, writeMetadata, pbfConfig, tboConfig, keepTmp);
		query.execute();
	}

}

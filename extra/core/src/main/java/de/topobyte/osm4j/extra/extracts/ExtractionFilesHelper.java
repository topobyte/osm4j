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

package de.topobyte.osm4j.extra.extracts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractionFilesHelper
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_SIMPLE_RELATIONS = "simple-relations";
	private static final String OPTION_COMPLEX_RELATIONS = "complex-relations";
	private static final String OPTION_SIMPLE_RELATIONS_BBOXES = "simple-relations-bboxes";
	private static final String OPTION_COMPLEX_RELATIONS_BBOXES = "complex-relations-bboxes";
	private static final String OPTION_FILE_NAMES_TREE_NODES = "tree-nodes";
	private static final String OPTION_FILE_NAMES_TREE_WAYS = "tree-ways";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE = "tree-simple-relations";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX = "tree-complex-relations";
	private static final String OPTION_FILE_NAMES_RELATION_NODES = "relation-nodes";
	private static final String OPTION_FILE_NAMES_RELATION_WAYS = "relation-ways";
	private static final String OPTION_FILE_NAMES_RELATION_RELATIONS = "relation-relations";

	public static void addOptions(Options options)
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_TREE, true, false, "relative path to the data tree");
		OptionHelper.addL(options, OPTION_SIMPLE_RELATIONS, true, false, "relative path to simple relation batches");
		OptionHelper.addL(options, OPTION_COMPLEX_RELATIONS, true, false, "relative path to complex relation batches");
		OptionHelper.addL(options, OPTION_SIMPLE_RELATIONS_BBOXES, true, false, "relative path to simple relation batches bboxes");
		OptionHelper.addL(options, OPTION_COMPLEX_RELATIONS_BBOXES, true, false, "relative path to complex relation batches bboxes");
		OptionHelper.addL(options, OPTION_FILE_NAMES_TREE_NODES, true, false, "name of node files in tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_TREE_WAYS, true, false, "name of way files in tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE, true, false, "name of simple relations in tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX, true, false, "name of complex relations in tree");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATION_NODES, true, false, "name of node files in relation batches");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATION_WAYS, true, false, "name of way files in relation batches");
		OptionHelper.addL(options, OPTION_FILE_NAMES_RELATION_RELATIONS, true, false, "name of relation files in relation batches");
		// @formatter:on
	}

	public static void parse(CommandLine line, ExtractionFileNames fileNames)
	{
		TreeFileNames treeNames = fileNames.getTreeNames();
		BatchFileNames relationNames = fileNames.getRelationNames();

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
			treeNames.setNodes(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_NODES));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_WAYS)) {
			treeNames.setWays(line.getOptionValue(OPTION_FILE_NAMES_TREE_WAYS));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE)) {
			treeNames.setSimpleRelations(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_SIMPLE));
		}
		if (line.hasOption(OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX)) {
			treeNames.setComplexRelations(line
					.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS_COMPLEX));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_NODES)) {
			relationNames.setNodes(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_NODES));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_WAYS)) {
			relationNames.setWays(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_WAYS));
		}
		if (line.hasOption(OPTION_FILE_NAMES_RELATION_RELATIONS)) {
			relationNames.setRelations(line
					.getOptionValue(OPTION_FILE_NAMES_RELATION_RELATIONS));
		}
	}

}

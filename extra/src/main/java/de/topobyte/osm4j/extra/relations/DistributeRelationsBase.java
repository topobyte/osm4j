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

package de.topobyte.osm4j.extra.relations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxListOutputStream;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class DistributeRelationsBase extends
		AbstractExecutableInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_NODES = "nodes";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS = "tree_relations";
	private static final String OPTION_OUTPUT_EMPTY_RELATIONS = "empty_relations";
	private static final String OPTION_OUTPUT_NON_TREE_RELATIONS = "non_tree_relations";
	private static final String OPTION_OUTPUT_NON_TREE_BBOXES = "non_tree_bboxes";

	protected String pathTree;
	protected String pathData;
	protected String pathOutputEmpty;
	protected String pathOutputNonTree;
	protected String pathOutputBboxes;

	protected Path dirData;
	protected String fileNamesRelations;
	protected String fileNamesWays;
	protected String fileNamesNodes;
	protected String fileNamesTreeRelations;

	protected DataTree tree;
	protected List<Path> subdirs;

	protected DataTreeFiles treeFilesRelations;

	protected OsmStreamOutput outputEmpty;
	protected OsmStreamOutput outputNonTree;
	protected Map<Node, OsmStreamOutput> outputs = new HashMap<>();

	protected IdBboxListOutputStream outputBboxes;

	public DistributeRelationsBase()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_TREE, true, true, "tree to use for small relations");
		OptionHelper.add(options, OPTION_DIRECTORY, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relations files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the ways files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES, true, true, "names of the nodes files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS, true, true, "names of the relation files in the tree");
		OptionHelper.add(options, OPTION_OUTPUT_EMPTY_RELATIONS, true, true, "where to store relations without geometry");
		OptionHelper.add(options, OPTION_OUTPUT_NON_TREE_RELATIONS, true, true, "where to store relations not matched with the tree");
		OptionHelper.add(options, OPTION_OUTPUT_NON_TREE_BBOXES, true, true, "where to store bboxes of non-matched relations");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathData = line.getOptionValue(OPTION_DIRECTORY);
		pathTree = line.getOptionValue(OPTION_TREE);
		pathOutputEmpty = line.getOptionValue(OPTION_OUTPUT_EMPTY_RELATIONS);
		pathOutputNonTree = line
				.getOptionValue(OPTION_OUTPUT_NON_TREE_RELATIONS);
		pathOutputBboxes = line.getOptionValue(OPTION_OUTPUT_NON_TREE_BBOXES);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesNodes = line.getOptionValue(OPTION_FILE_NAMES_NODES);

		fileNamesTreeRelations = line
				.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS);
	}

}

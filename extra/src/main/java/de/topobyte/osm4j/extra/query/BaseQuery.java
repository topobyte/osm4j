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

package de.topobyte.osm4j.extra.query;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.predicate.ContainmentTest;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class BaseQuery extends AbstractExecutableInputOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_TMP = "tmp";
	private static final String OPTION_TREE = "tree";
	private static final String OPTION_SIMPLE_RELATIONS = "simple_relations";
	private static final String OPTION_COMPLEX_RELATIONS = "complex_relations";
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
		OptionHelper.add(options, OPTION_TREE, true, true, "path to the data tree");
		OptionHelper.add(options, OPTION_SIMPLE_RELATIONS, true, true, "path to simple relation batches");
		OptionHelper.add(options, OPTION_COMPLEX_RELATIONS, true, true, "path to complex relation batches");
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
	protected DataTree tree;

	protected String fileNamesTreeNodes;
	protected String fileNamesTreeWays;
	protected String fileNamesTreeSimpleRelations;
	protected String fileNamesTreeComplexRelations;
	protected String fileNamesRelationNodes;
	protected String fileNamesRelationWays;
	protected String fileNamesRelationRelations;

	protected Envelope queryEnvelope;
	protected ContainmentTest test;

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
	}

	protected void execute() throws IOException
	{
		if (pathTmp == null) {
			pathTmp = Files.createTempDirectory("extract");
		}
		System.out.println(pathTmp);
		Files.createDirectories(pathTmp);
		if (!Files.isDirectory(pathTmp)) {
			System.out
					.println("Unable to create temporary directory for intermediate files");
			System.exit(1);
		}
		if (pathTmp.toFile().listFiles().length != 0) {
			System.out
					.println("Temporary directory for intermediate files is not empty");
			System.exit(1);
		}
		System.out.println("Storing intermediate files here: " + pathTmp);

		DataTree tree = DataTreeOpener.open(pathTree.toFile());
		GeometryFactory factory = new GeometryFactory();
		Geometry box = factory.toGeometry(queryEnvelope);
		List<Node> leafs = tree.query(box);

		DataTreeFiles filesTreeNodes = new DataTreeFiles(pathTree.toFile(),
				fileNamesTreeNodes);

		int n = 0;
		for (Node leaf : leafs) {
			System.out.println("Loading data from leaf: "
					+ Long.toHexString(leaf.getPath()));
			File fileNodes = filesTreeNodes.getFile(leaf);
			InputStream input = StreamUtil.bufferedInputStream(fileNodes);
			OsmIterator iterator = OsmIoUtils.setupOsmIterator(input,
					inputFormat, readMetadata);
			InMemoryDataSet data = DataSetReader.read(iterator, true, true,
					true);

			int m = 0;
			for (OsmNode node : data.getNodes().valueCollection()) {
				if (test.contains(new Coordinate(node.getLongitude(), node
						.getLatitude()))) {
					m++;
				}
			}
			System.out.println(String.format("Found %d nodes", m));
			n += m;
		}

		System.out.println(String.format("Total number of nodes: %d", n));
	}
}

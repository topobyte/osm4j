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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.predicate.ContainmentTest;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.extra.OsmOutput;
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
		// Make sure a temporary directory is available

		if (pathTmp == null) {
			pathTmp = Files.createTempDirectory("extract");
		}
		System.out.println("Temporary directory: " + pathTmp);
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

		// Create sub-directories for intermediate files

		Path pathTmpNodes = pathTmp.resolve("nodes");
		Path pathTmpWays = pathTmp.resolve("ways");
		Path pathTmpSimpleRelations = pathTmp.resolve("relations.simple");
		Path pathTmpComplexRelations = pathTmp.resolve("relations.complex");

		Files.createDirectory(pathTmpNodes);
		Files.createDirectory(pathTmpWays);
		Files.createDirectory(pathTmpSimpleRelations);
		Files.createDirectory(pathTmpComplexRelations);

		// Query setup

		DataTree tree = DataTreeOpener.open(pathTree.toFile());
		GeometryFactory factory = new GeometryFactory();
		Geometry box = factory.toGeometry(queryEnvelope);
		List<Node> leafs = tree.query(box);

		DataTreeFiles filesTreeNodes = new DataTreeFiles(pathTree,
				fileNamesTreeNodes);
		DataTreeFiles filesTreeWays = new DataTreeFiles(pathTree,
				fileNamesTreeWays);
		DataTreeFiles filesTreeSimpleRelations = new DataTreeFiles(pathTree,
				fileNamesTreeSimpleRelations);
		DataTreeFiles filesTreeComplexRelations = new DataTreeFiles(pathTree,
				fileNamesTreeComplexRelations);

		// Lists of files that need to be merged in the end

		List<Path> pathsNodes = new ArrayList<>();
		List<Path> pathsWays = new ArrayList<>();
		List<Path> pathsSimpleRelations = new ArrayList<>();
		List<Path> pathsComplexRelations = new ArrayList<>();

		// Query data tree

		int nNodes = 0;
		int nWays = 0;
		int nSimpleRelations = 0;
		int nComplexRelations = 0;

		int tmpIndex = 0;

		for (Node leaf : leafs) {
			String leafName = Long.toHexString(leaf.getPath());

			if (test.contains(leaf.getEnvelope())) {
				System.out.println("Leaf is completely contained: " + leafName);
				pathsNodes.add(filesTreeNodes.getPath(leaf));
				pathsWays.add(filesTreeWays.getPath(leaf));
				pathsSimpleRelations
						.add(filesTreeSimpleRelations.getPath(leaf));
				pathsComplexRelations.add(filesTreeComplexRelations
						.getPath(leaf));
				continue;
			}

			System.out.println("Loading data from leaf: " + leafName);
			InMemoryDataSet dataNodes = read(filesTreeNodes.getPath(leaf));
			InMemoryDataSet dataWays = read(filesTreeWays.getPath(leaf));
			InMemoryDataSet dataSimpleRelations = read(filesTreeSimpleRelations
					.getPath(leaf));

			tmpIndex++;
			String tmpFilenames = String.format("%d%s", tmpIndex,
					OsmIoUtils.extension(outputFormat));
			Path pathOutNodes = pathTmpNodes.resolve(tmpFilenames);
			Path pathOutWays = pathTmpWays.resolve(tmpFilenames);
			Path pathOutSimpleRelations = pathTmpSimpleRelations
					.resolve(tmpFilenames);
			Path pathOutComplexRelations = pathTmpComplexRelations
					.resolve(tmpFilenames);

			OsmOutput outNodes = createOutput(pathOutNodes);
			OsmOutput outWays = createOutput(pathOutWays);
			OsmOutput outSimpleRelations = createOutput(pathOutSimpleRelations);
			OsmOutput outComplexRelations = createOutput(pathOutComplexRelations);

			TLongSet nodeIds = new TLongHashSet();
			TLongSet wayIds = new TLongHashSet();

			for (OsmNode node : dataNodes.getNodes().valueCollection()) {
				if (test.contains(new Coordinate(node.getLongitude(), node
						.getLatitude()))) {
					nodeIds.add(node.getId());
					outNodes.getOsmOutput().write(node);
				}
			}

			for (OsmWay way : dataWays.getWays().valueCollection()) {
				boolean in = false;
				for (int i = 0; i < way.getNumberOfNodes(); i++) {
					if (nodeIds.contains(way.getNodeId(i))) {
						in = true;
						break;
					}
				}
				if (!in) {
					// TODO: test geometry-wise
				}
				if (in) {
					wayIds.add(way.getId());
					outWays.getOsmOutput().write(way);
				}
			}

			int nSimple = 0;
			for (OsmRelation relation : dataSimpleRelations.getRelations()
					.valueCollection()) {
				boolean in = false;
				for (int i = 0; i < relation.getNumberOfMembers(); i++) {
					OsmRelationMember member = relation.getMember(i);
					if (member.getType() == EntityType.Node
							&& nodeIds.contains(member.getId())
							|| member.getType() == EntityType.Way
							&& wayIds.contains(member.getId())) {
						in = true;
						break;
					}
				}
				if (!in) {
					// TODO: test geometry-wise
				}
				if (in) {
					outSimpleRelations.getOsmOutput().write(relation);
					nSimple++;
				}
			}

			int nComplex = 0;

			System.out.println(String.format("Found %d nodes", nodeIds.size()));
			System.out.println(String.format("Found %d ways", wayIds.size()));
			System.out.println(String.format("Found %d simple relations",
					nSimple));
			System.out.println(String.format("Found %d complex relations",
					nComplex));
			nNodes += nodeIds.size();
			nWays += wayIds.size();
			nSimpleRelations += nSimple;
			nComplexRelations += nComplex;

			finish(outNodes);
			finish(outWays);
			finish(outSimpleRelations);
			finish(outComplexRelations);
		}

		System.out.println(String.format("Total number of nodes: %d", nNodes));
		System.out.println(String.format("Total number of ways: %d", nWays));
		System.out.println(String.format(
				"Total number of simple relations: %d", nSimpleRelations));
		System.out.println(String.format(
				"Total number of complex relations: %d", nComplexRelations));

		FileUtils.deleteDirectory(pathTmp.toFile());
	}

	private InMemoryDataSet read(Path path) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path);
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);
		InMemoryDataSet data = DataSetReader.read(iterator, true, true, true);
		input.close();
		return data;
	}

	private OsmOutput createOutput(Path path) throws IOException
	{
		OutputStream outputStream = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutputStream = OsmIoUtils
				.setupOsmOutput(outputStream, outputFormat, writeMetadata,
						pbfConfig, tboConfig);
		return new OsmOutput(outputStream, osmOutputStream);
	}

	private void finish(OsmOutput osmOutput) throws IOException
	{
		osmOutput.getOsmOutput().complete();
		osmOutput.getOutputStream().close();
	}

}

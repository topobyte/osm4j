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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.core.resolve.CompositeOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.core.resolve.NullOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.AbstractTaskInputOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class DistributeSimpleRelations extends AbstractTaskInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_NODES = "nodes";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS = "tree_relations";
	private static final String OPTION_OUTPUT_EMPTY_RELATIONS = "empty_relations";
	private static final String OPTION_OUTPUT_NON_TREE_RELATIONS = "non_tree_relations";

	@Override
	protected String getHelpMessage()
	{
		return DistributeSimpleRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		DistributeSimpleRelations task = new DistributeSimpleRelations();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathTree;
	private String pathData;
	private String pathOutputEmpty;
	private String pathOutputNonTree;

	private Path dirData;
	private String fileNamesRelations;
	private String fileNamesWays;
	private String fileNamesNodes;
	private String fileNamesTreeRelations;

	private DataTree tree;
	private List<Path> subdirs;

	private DataTreeFiles treeFilesRelations;

	private Output outputEmpty;
	private Output outputNonTree;
	private Map<Node, Output> outputs = new HashMap<>();

	public DistributeSimpleRelations()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_TREE, true, true, "tree to use for small relations");
		OptionHelper.add(options, OPTION_DIRECTORY, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relations files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the ways files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES, true, true, "names of the nodes files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS, true, true, "names of the relation files in the tree");
		OptionHelper.add(options, OPTION_OUTPUT_EMPTY_RELATIONS, true, true, "where to store relations without geometry");
		OptionHelper.add(options, OPTION_OUTPUT_NON_TREE_RELATIONS, true, true, "where to store relations without geometry");
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

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesNodes = line.getOptionValue(OPTION_FILE_NAMES_NODES);

		fileNamesTreeRelations = line
				.getOptionValue(OPTION_FILE_NAMES_TREE_RELATIONS);
	}

	protected void init() throws IOException
	{
		dirData = Paths.get(pathData);

		if (!Files.isDirectory(dirData)) {
			System.out.println("Input path is not a directory");
			System.exit(1);
		}

		File dirTree = new File(pathTree);
		tree = DataTreeOpener.open(dirTree);

		treeFilesRelations = new DataTreeFiles(dirTree, fileNamesTreeRelations);

		subdirs = new ArrayList<>();
		File[] subs = dirData.toFile().listFiles();
		for (File sub : subs) {
			if (!sub.isDirectory()) {
				continue;
			}
			Path subPath = sub.toPath();
			Path relations = subPath.resolve(fileNamesRelations);
			Path ways = subPath.resolve(fileNamesWays);
			Path nodes = subPath.resolve(fileNamesNodes);
			if (!Files.exists(relations) || !Files.exists(ways)
					|| !Files.exists(nodes)) {
				continue;
			}
			subdirs.add(subPath);
		}

		Collections.sort(subdirs, new Comparator<Path>() {

			@Override
			public int compare(Path o1, Path o2)
			{
				String name1 = o1.getFileName().toString();
				String name2 = o2.getFileName().toString();
				try {
					int n1 = Integer.parseInt(name1);
					int n2 = Integer.parseInt(name2);
					return Integer.compare(n1, n2);
				} catch (NumberFormatException e) {
					// compare as paths
				}
				return o1.compareTo(o2);
			}
		});

		// Setup output for non-geometry relations

		File fileOutputEmpty = new File(pathOutputEmpty);
		OutputStream outEmpty = new BufferedOutputStream(new FileOutputStream(
				fileOutputEmpty));
		OsmOutputStream osmOutputEmpty = OsmIoUtils.setupOsmOutput(outEmpty,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		outputEmpty = new Output(fileOutputEmpty.toPath(), outEmpty,
				osmOutputEmpty);

		// Setup output for non-tree relations

		File fileOutputNonTree = new File(pathOutputNonTree);
		OutputStream outNonTree = new BufferedOutputStream(
				new FileOutputStream(fileOutputNonTree));
		OsmOutputStream osmOutputNonTree = OsmIoUtils.setupOsmOutput(
				outNonTree, outputFormat, writeMetadata, pbfConfig, tboConfig);
		outputNonTree = new Output(fileOutputNonTree.toPath(), outNonTree,
				osmOutputNonTree);

		// Setup output for tree relations

		ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : tree.getLeafs()) {
			File file = treeFilesRelations.getFile(leaf);
			OutputStream out = new BufferedOutputStream(factory.create(file));
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(out,
					outputFormat, writeMetadata, pbfConfig, tboConfig);

			if (osmOutput instanceof TboWriter) {
				TboWriter tboWriter = (TboWriter) osmOutput;
				tboWriter.setBatchSizeRelationsByMembers(1024);
			}

			outputs.put(leaf, new Output(file.toPath(), out, osmOutput));

			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}
	}

	int nWrittenEmpty = 0;
	int nWrittenToTree = 0;
	int nRemaining = 0;

	private void execute() throws IOException
	{
		int i = 0;
		for (Path path : subdirs) {
			System.out.println(String.format("Processing directory %d of %d",
					++i, subdirs.size()));
			build(path);
			System.out.println(String.format(
					"empty: %d, tree: %d, remaining: %d", nWrittenEmpty,
					nWrittenToTree, nRemaining));
		}
	}

	private void finish() throws IOException
	{
		outputEmpty.getOsmOutput().complete();
		outputEmpty.getOutputStream().close();

		outputNonTree.getOsmOutput().complete();
		outputNonTree.getOutputStream().close();

		for (Output output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

	private void build(Path path) throws IOException
	{
		Path pathRelations = path.resolve(fileNamesRelations);
		Path pathWays = path.resolve(fileNamesWays);
		Path pathNodes = path.resolve(fileNamesNodes);

		InMemoryDataSet dataWays = read(pathWays);
		InMemoryDataSet dataNodes = read(pathNodes);

		OsmEntityProvider entityProvider = new CompositeOsmEntityProvider(
				dataNodes, dataWays, new NullOsmEntityProvider());

		InputStream input = StreamUtil.bufferedInputStream(pathRelations
				.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, writeMetadata);
		RelationIterator relationIterator = new RelationIterator(osmIterator);

		for (OsmRelation relation : relationIterator) {
			Set<OsmNode> nodes;
			try {
				nodes = RelationUtil.findNodes(relation, entityProvider);
			} catch (EntityNotFoundException e) {
				continue;
			}

			if (nodes.size() == 0) {
				nWrittenEmpty++;
				write(relation, outputEmpty);
				continue;
			}

			Geometry box = box(nodes);
			List<Node> leafs = tree.query(box);

			if (leafs.size() == 1) {
				nWrittenToTree++;
				write(relation, outputs.get(leafs.get(0)));
			} else {
				nRemaining++;
				write(relation, outputNonTree);
			}
		}
	}

	private void write(OsmRelation relation, Output output) throws IOException
	{
		output.getOsmOutput().write(relation);
	}

	private Geometry box(Set<OsmNode> nodes)
	{
		Envelope env = new Envelope();
		for (OsmNode node : nodes) {
			env.expandToInclude(node.getLongitude(), node.getLatitude());
		}
		return new GeometryFactory().toGeometry(env);
	}

	private InMemoryDataSet read(Path path) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, false);
		InMemoryDataSet data = DataSetReader.read(osmIterator, false, false,
				false);
		input.close();
		return data;
	}

}

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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.extra.OsmOutput;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxListOutputStream;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
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

	protected OsmOutput outputEmpty;
	protected OsmOutput outputNonTree;
	protected Map<Node, OsmOutput> outputs = new HashMap<>();

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
		OutputStream outEmpty = StreamUtil
				.bufferedOutputStream(fileOutputEmpty);
		OsmOutputStream osmOutputEmpty = OsmIoUtils.setupOsmOutput(outEmpty,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		outputEmpty = new OsmOutput(outEmpty, osmOutputEmpty);

		// Setup output for non-tree relations

		File fileOutputNonTree = new File(pathOutputNonTree);
		OutputStream outNonTree = StreamUtil
				.bufferedOutputStream(fileOutputNonTree);
		OsmOutputStream osmOutputNonTree = OsmIoUtils.setupOsmOutput(
				outNonTree, outputFormat, writeMetadata, pbfConfig, tboConfig);
		outputNonTree = new OsmOutput(outNonTree, osmOutputNonTree);

		// Setup output for non-tree relations' bboxes

		OutputStream outBboxes = StreamUtil
				.bufferedOutputStream(pathOutputBboxes);
		outputBboxes = new IdBboxListOutputStream(outBboxes);

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

			outputs.put(leaf, new OsmOutput(out, osmOutput));

			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}
	}

	int nWrittenEmpty = 0;
	int nWrittenToTree = 0;
	int nRemaining = 0;

	protected void execute() throws IOException
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

	protected void finish() throws IOException
	{
		outputEmpty.getOsmOutput().complete();
		outputEmpty.getOutputStream().close();

		outputNonTree.getOsmOutput().complete();
		outputNonTree.getOutputStream().close();

		outputBboxes.close();

		for (OsmOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

	protected abstract void build(Path path) throws IOException;

	protected Envelope box(Collection<OsmNode> nodes)
	{
		Envelope env = new Envelope();
		for (OsmNode node : nodes) {
			env.expandToInclude(node.getLongitude(), node.getLatitude());
		}
		return env;
	}

	protected Geometry box(Envelope env)
	{
		return new GeometryFactory().toGeometry(env);
	}

	protected InMemoryDataSet read(Path path, boolean readMetadata,
			boolean keepTags) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, readMetadata);
		InMemoryDataSet data = DataSetReader.read(osmIterator, keepTags,
				keepTags, keepTags);
		input.close();
		return data;
	}

}

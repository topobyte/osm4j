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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;

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
import java.util.Collection;
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
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.AbstractTaskInputOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class DistributeRelations extends AbstractTaskInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_NODES = "nodes";
	private static final String OPTION_FILE_NAMES_TREE_RELATIONS = "tree_relations";
	private static final String OPTION_OUTPUT_EMPTY_RELATIONS = "empty_relations";

	@Override
	protected String getHelpMessage()
	{
		return DistributeRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		DistributeRelations task = new DistributeRelations();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathTree;
	private String pathData;
	private String pathOutputEmpty;

	private Path dirData;
	private String fileNamesRelations;
	private String fileNamesWays;
	private String fileNamesNodes;
	private String fileNamesTreeRelations;

	private DataTree tree;
	private List<Path> subdirs;

	private DataTreeFiles treeFilesRelations;

	private Output outputEmpty;
	private Map<Node, Output> outputs = new HashMap<>();

	public DistributeRelations()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_TREE, true, true, "tree to use for small relations");
		OptionHelper.add(options, OPTION_DIRECTORY, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relations files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the ways files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES, true, true, "names of the nodes files in each directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_TREE_RELATIONS, true, true, "names of the relation files in the tree");
		OptionHelper.add(options, OPTION_OUTPUT_EMPTY_RELATIONS, true, true, "where to store relations without geometry");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathData = line.getOptionValue(OPTION_DIRECTORY);
		pathTree = line.getOptionValue(OPTION_TREE);
		pathOutputEmpty = line.getOptionValue(OPTION_OUTPUT_EMPTY_RELATIONS);

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

		// Setup output for tree relations

		ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : tree.getLeafs()) {
			File file = treeFilesRelations.getFile(leaf);
			OutputStream out = new BufferedOutputStream(factory.create(file));
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(out,
					outputFormat, writeMetadata, pbfConfig, tboConfig);
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

		InMemoryDataSet dataRelations = read(pathRelations);
		InMemoryDataSet dataWays = read(pathWays);
		InMemoryDataSet dataNodes = read(pathNodes);

		OsmEntityProvider entityProvider = new CompositeOsmEntityProvider(
				dataNodes, dataWays, dataRelations);

		RelationGraph relationGraph = new RelationGraph(true, false);
		relationGraph.build(dataRelations);

		List<Group> groups = relationGraph.buildGroups();
		TLongSet idsSimpleRelations = relationGraph.getIdsSimpleRelations();

		List<RelationGroup> relationGroups = new ArrayList<>();

		for (Group group : groups) {
			try {
				List<OsmRelation> groupRelations = findRelations(
						group.getRelationIds(), dataRelations);
				relationGroups.add(new RelationGroupMultiple(groupRelations));
			} catch (EntityNotFoundException e) {
				System.out.println("unable to build relation group");
			}
		}

		TLongIterator simpleRelations = idsSimpleRelations.iterator();
		while (simpleRelations.hasNext()) {
			long id = simpleRelations.next();
			OsmRelation relation = dataRelations.getRelations().get(id);
			relationGroups.add(new RelationGroupSingle(relation));
		}

		for (RelationGroup group : relationGroups) {
			try {
				Set<OsmNode> nodes = group.findNodes(entityProvider);
				if (nodes.size() == 0) {
					nWrittenEmpty += group.getRelations().size();
					write(group, outputEmpty);
					continue;
				}
				Geometry box = box(nodes);
				List<Node> leafs = tree.query(box);
				if (leafs.size() == 1) {
					nWrittenToTree += group.getRelations().size();
					Node leaf = leafs.get(0);
					Output output = outputs.get(leaf);
					write(group, output);
				} else {
					nRemaining += group.getRelations().size();
					// TODO: write somewhere else with ways and nodes
				}
			} catch (EntityNotFoundException e) {
				System.out.println("unable to build simple relation");
			}
		}
	}

	private void write(RelationGroup group, Output output) throws IOException
	{
		Collection<OsmRelation> relations = group.getRelations();
		for (OsmRelation relation : relations) {
			output.getOsmOutput().write(relation);
		}
	}

	private List<OsmRelation> findRelations(TLongSet ids,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		List<OsmRelation> relations = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			relations.add(entityProvider.getRelation(idIterator.next()));
		}
		return relations;
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
				inputFormat, writeMetadata);
		InMemoryDataSet data = DataSetReader
				.read(osmIterator, true, true, true);
		input.close();
		return data;
	}

}

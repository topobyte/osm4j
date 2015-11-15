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

package de.topobyte.osm4j.extra.datatree.ways;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vividsolutions.jts.geom.LineString;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.core.resolve.UnionOsmEntityProvider;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.utils.AbstractExecutableInputOutput;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class DistributeWays extends AbstractExecutableInputOutput
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_NODES1 = "nodes1";
	private static final String OPTION_FILE_NAMES_NODES2 = "nodes2";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT_WAYS = "ways_out";
	private static final String OPTION_FILE_NAMES_OUTPUT_NODES = "nodes_out";

	@Override
	protected String getHelpMessage()
	{
		return DistributeWays.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		DistributeWays task = new DistributeWays();

		task.setup(args);

		task.prepare();

		task.execute();
	}

	private String pathTree;

	private String fileNamesNodes1;
	private String fileNamesNodes2;
	private String fileNamesWays;
	private String fileNamesOutputWays;
	private String fileNamesOutputNodes;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	public DistributeWays()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES1, true, true, "names of the node files in the tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES2, true, true, "names of the node files in the tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the way files in the tree");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT_WAYS, true, true, "name of files for intersecting ways");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT_NODES, true, true, "name of files for intersecting ways' nodes");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		inputFormatNodes = inputFormat;
		inputFormatWays = inputFormat;

		fileNamesNodes1 = line.getOptionValue(OPTION_FILE_NAMES_NODES1);
		fileNamesNodes2 = line.getOptionValue(OPTION_FILE_NAMES_NODES2);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesOutputWays = line
				.getOptionValue(OPTION_FILE_NAMES_OUTPUT_WAYS);
		fileNamesOutputNodes = line
				.getOptionValue(OPTION_FILE_NAMES_OUTPUT_NODES);

		pathTree = line.getOptionValue(OPTION_TREE);
	}

	private DataTree tree;
	private File dirTree;
	private List<Node> leafs;
	private Map<Node, Output> outputsWays = new HashMap<>();
	private Map<Node, Output> outputsNodes = new HashMap<>();

	private long counter = 0;
	private long noneFound = 0;
	private long unableToBuild = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
	private ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

	public void prepare() throws IOException
	{
		tree = DataTreeOpener.open(new File(pathTree));
		dirTree = new File(pathTree);
		leafs = tree.getLeafs();

		DataTreeFiles filesWays = new DataTreeFiles(dirTree,
				fileNamesOutputWays);
		DataTreeFiles filesNodes = new DataTreeFiles(dirTree,
				fileNamesOutputNodes);

		for (Node leaf : leafs) {
			Output outputWays = createOutput(filesWays.getFile(leaf));
			outputsWays.put(leaf, outputWays);
			Output outputNodes = createOutput(filesNodes.getFile(leaf));
			outputsNodes.put(leaf, outputNodes);
		}
	}

	private Output createOutput(File file) throws IOException
	{
		OutputStream output = factory.create(file);
		output = new BufferedOutputStream(output);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		return new Output(file.toPath(), output, osmOutput);
	}

	public void execute() throws IOException
	{
		DataTreeFiles filesNodes1 = new DataTreeFiles(dirTree, fileNamesNodes1);
		DataTreeFiles filesNodes2 = new DataTreeFiles(dirTree, fileNamesNodes2);
		DataTreeFiles filesWays = new DataTreeFiles(dirTree, fileNamesWays);

		int i = 0;
		for (Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			File fileNodes1 = filesNodes1.getFile(leaf);
			File fileNodes2 = filesNodes2.getFile(leaf);
			File fileWays = filesWays.getFile(leaf);

			InputStream inputNodes1 = StreamUtil
					.bufferedInputStream(fileNodes1);
			InputStream inputNodes2 = StreamUtil
					.bufferedInputStream(fileNodes2);
			InputStream inputWays = StreamUtil.bufferedInputStream(fileWays);

			long nodesSize1 = fileNodes1.length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize1 / 1024. / 1024.));

			InMemoryDataSet dataNodes1 = DataSetReader.read(OsmIoUtils
					.setupOsmIterator(inputNodes1, inputFormatNodes, false),
					false, false, false);

			long nodesSize2 = fileNodes2.length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize2 / 1024. / 1024.));

			InMemoryDataSet dataNodes2 = DataSetReader.read(OsmIoUtils
					.setupOsmIterator(inputNodes2, inputFormatNodes, false),
					false, false, false);

			long waysSize = fileWays.length();
			System.out.println(String.format(
					"Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));

			InMemoryDataSet dataWays = DataSetReader.read(
					OsmIoUtils.setupOsmIterator(inputWays, inputFormatWays,
							writeMetadata), false, true, false);

			inputNodes1.close();
			inputNodes2.close();
			inputWays.close();

			System.out.println("Number of ways: " + dataWays.getWays().size());

			List<OsmEntityProvider> providers = new ArrayList<>();
			providers.add(dataNodes1);
			providers.add(dataNodes2);
			UnionOsmEntityProvider entityProvider = new UnionOsmEntityProvider(
					providers);

			long[] wayIds = dataWays.getWays().keys();
			Arrays.sort(wayIds);
			TLongObjectMap<OsmWay> ways = dataWays.getWays();
			for (long id : wayIds) {
				OsmWay way = ways.get(id);
				build(leaf, way, entityProvider);
			}

			stats(i);
		}

		for (Output output : outputsWays.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
		for (Output output : outputsNodes.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

	private void build(Node leaf, OsmWay way,
			UnionOsmEntityProvider entityProvider) throws IOException
	{
		TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
		List<Node> leafs;
		if (way.getNumberOfNodes() == 1) {
			try {
				long nodeId = way.getNodeId(0);
				OsmNode node = entityProvider.getNode(nodeId);
				nodes.put(nodeId, node);
				leafs = tree.query(node.getLongitude(), node.getLatitude());
			} catch (EntityNotFoundException e) {
				System.out.println("Entity not found while building way: "
						+ way.getId());
				return;
			}
		} else {
			try {
				LineString line = GeometryBuilder.build(way, entityProvider);
				putNodes(way, nodes, entityProvider);
				leafs = tree.query(line);
			} catch (EntityNotFoundException e) {
				System.out.println("Entity not found while building way: "
						+ way.getId());
				return;
			}
		}

		for (Node ileaf : leafs) {
			if (ileaf == leaf) {
				continue;
			}
			outputsWays.get(ileaf).getOsmOutput().write(way);
			for (OsmNode node : nodes.valueCollection()) {
				outputsNodes.get(ileaf).getOsmOutput().write(node);
			}
		}

		if (leafs.size() == 0) {
			System.out.println("No leaf found for way: " + way.getId());
		}

		counter++;
	}

	private void putNodes(OsmWay way, TLongObjectMap<OsmNode> nodes,
			UnionOsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			nodes.put(nodeId, entityProvider.getNode(nodeId));
		}
	}

	private void stats(int leafsDone)
	{
		System.out.println(String.format(
				"ways: %s, no leafs found: %s, unable to build: %s",
				format.format(counter), format.format(noneFound),
				format.format(unableToBuild)));

		long now = System.currentTimeMillis();
		long past = now - start;
		long estimate = Math.round((past / (double) leafsDone) * leafs.size());
		System.out.println(String.format("Past: %.2f", past / 1000 / 60.));
		System.out.println(String.format("Estimate: %.2f",
				estimate / 1000 / 60.));
	}

}

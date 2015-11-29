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
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vividsolutions.jts.geom.LineString;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.core.resolve.UnionOsmEntityProvider;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public class WaysDistributor
{

	private Path pathTree;

	private String fileNamesNodes1;
	private String fileNamesNodes2;
	private String fileNamesWays;
	private String fileNamesOutputWays;
	private String fileNamesOutputNodes;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	private OsmOutputConfig outputConfig;

	public WaysDistributor(Path pathTree, String fileNamesNodes1,
			String fileNamesNodes2, String fileNamesWays,
			String fileNamesOutputWays, String fileNamesOutputNodes,
			FileFormat inputFormatNodes, FileFormat inputFormatWays,
			OsmOutputConfig outputConfig)
	{
		this.pathTree = pathTree;
		this.fileNamesNodes1 = fileNamesNodes1;
		this.fileNamesNodes2 = fileNamesNodes2;
		this.fileNamesWays = fileNamesWays;
		this.fileNamesOutputWays = fileNamesOutputWays;
		this.fileNamesOutputNodes = fileNamesOutputNodes;
		this.inputFormatNodes = inputFormatNodes;
		this.inputFormatWays = inputFormatWays;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		prepare();

		run();
	}

	private DataTree tree;
	private List<Node> leafs;
	private Map<Node, OsmStreamOutput> outputsWays = new HashMap<>();
	private Map<Node, OsmStreamOutput> outputsNodes = new HashMap<>();

	private long counter = 0;
	private long noneFound = 0;
	private long unableToBuild = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
	private ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

	private void prepare() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());
		leafs = tree.getLeafs();

		DataTreeFiles filesWays = new DataTreeFiles(pathTree,
				fileNamesOutputWays);
		DataTreeFiles filesNodes = new DataTreeFiles(pathTree,
				fileNamesOutputNodes);

		for (Node leaf : leafs) {
			OsmStreamOutput outputWays = createOutput(filesWays.getFile(leaf));
			outputsWays.put(leaf, outputWays);
			OsmStreamOutput outputNodes = createOutput(filesNodes.getFile(leaf));
			outputsNodes.put(leaf, outputNodes);
		}
	}

	private OsmStreamOutput createOutput(File file) throws IOException
	{
		OutputStream output = factory.create(file);
		output = new BufferedOutputStream(output);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputConfig, true);
		return new OsmOutputStreamStreamOutput(output, osmOutput);
	}

	private void run() throws IOException
	{
		DataTreeFiles filesNodes1 = new DataTreeFiles(pathTree, fileNamesNodes1);
		DataTreeFiles filesNodes2 = new DataTreeFiles(pathTree, fileNamesNodes2);
		DataTreeFiles filesWays = new DataTreeFiles(pathTree, fileNamesWays);

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

			InMemoryListDataSet dataNodes1 = ListDataSetLoader.read(OsmIoUtils
					.setupOsmIterator(inputNodes1, inputFormatNodes,
							outputConfig.isWriteMetadata()), true, true, true);

			long nodesSize2 = fileNodes2.length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize2 / 1024. / 1024.));

			InMemoryListDataSet dataNodes2 = ListDataSetLoader.read(OsmIoUtils
					.setupOsmIterator(inputNodes2, inputFormatNodes,
							outputConfig.isWriteMetadata()), true, true, true);

			long waysSize = fileWays.length();
			System.out.println(String.format(
					"Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));

			InMemoryListDataSet dataWays = ListDataSetLoader.read(OsmIoUtils
					.setupOsmIterator(inputWays, inputFormatWays,
							outputConfig.isWriteMetadata()), true, true, true);

			inputNodes1.close();
			inputNodes2.close();
			inputWays.close();

			System.out.println("Number of ways: " + dataWays.getWays().size());

			List<OsmEntityProvider> providers = new ArrayList<>();
			providers.add(dataNodes1);
			providers.add(dataNodes2);
			UnionOsmEntityProvider entityProvider = new UnionOsmEntityProvider(
					providers);

			for (OsmWay way : dataWays.getWays()) {
				build(leaf, way, entityProvider);
			}

			stats(i);
		}

		for (OsmStreamOutput output : outputsWays.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
		for (OsmStreamOutput output : outputsNodes.values()) {
			output.getOsmOutput().complete();
			output.close();
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
			write(ileaf, way, nodes);
		}

		if (leafs.size() == 0) {
			System.out.println("No leaf found for way: " + way.getId());
		}

		counter++;
	}

	private void write(Node leaf, OsmWay way, TLongObjectMap<OsmNode> nodes)
			throws IOException
	{
		OsmStreamOutput wayOutput = outputsWays.get(leaf);
		OsmStreamOutput nodeOutput = outputsNodes.get(leaf);

		wayOutput.getOsmOutput().write(way);
		for (OsmNode node : nodes.valueCollection()) {
			nodeOutput.getOsmOutput().write(node);
		}
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

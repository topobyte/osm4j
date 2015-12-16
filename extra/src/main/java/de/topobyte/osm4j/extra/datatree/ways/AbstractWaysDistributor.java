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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.extra.QueryUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.geometry.GeometryGroup;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public abstract class AbstractWaysDistributor implements WaysDistributor
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

	public AbstractWaysDistributor(Path pathTree, String fileNamesNodes1,
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

	@Override
	public void execute() throws IOException
	{
		prepare();

		distribute();

		finish();
	}

	private DataTree tree;
	private List<Node> leafs;
	protected Map<Node, OsmStreamOutput> outputsWays = new HashMap<>();
	protected Map<Node, OsmStreamOutput> outputsNodes = new HashMap<>();

	private long counter = 0;
	private long noneFound = 0;
	private long unableToBuild = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
	private ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

	protected void prepare() throws IOException
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

	protected void finish() throws IOException
	{
		for (OsmStreamOutput output : outputsWays.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
		for (OsmStreamOutput output : outputsNodes.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

	protected abstract void leafData(LeafData leafData) throws IOException;

	protected abstract void write(Node leaf, OsmWay way,
			TLongObjectMap<OsmNode> nodes) throws IOException;

	protected boolean stopped = false;

	protected void distribute() throws IOException
	{
		DataTreeFiles filesNodes1 = new DataTreeFiles(pathTree, fileNamesNodes1);
		DataTreeFiles filesNodes2 = new DataTreeFiles(pathTree, fileNamesNodes2);
		DataTreeFiles filesWays = new DataTreeFiles(pathTree, fileNamesWays);

		int i = 0;
		Iterator<Node> iterator = leafs.iterator();
		while (!stopped && iterator.hasNext()) {
			Node leaf = iterator.next();
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

			leafData(new LeafData(leaf, dataWays, dataNodes1, dataNodes2));

			stats(i);
		}
	}

	protected void build(Node leaf, OsmWay way, OsmEntityProvider entityProvider)
			throws IOException
	{
		TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
		List<Node> leafs;
		try {
			if (way.getNumberOfNodes() == 1) {
				leafs = buildSingleNodeWay(way, nodes, entityProvider);
			} else if (way.getNumberOfNodes() < 4
					|| !OsmModelUtil.isClosed(way)) {
				leafs = buildNonClosedWay(way, nodes, entityProvider);
			} else {
				leafs = buildClosedWay(way, nodes, entityProvider);
			}
		} catch (EntityNotFoundException e) {
			System.out.println("Entity not found while building way: "
					+ way.getId());
			return;
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

	private List<Node> buildSingleNodeWay(OsmWay way,
			TLongObjectMap<OsmNode> nodes, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		long nodeId = way.getNodeId(0);
		OsmNode node = entityProvider.getNode(nodeId);
		nodes.put(nodeId, node);
		return tree.query(node.getLongitude(), node.getLatitude());
	}

	private GeometryFactory f = new GeometryFactory();
	private WayBuilder wb = new WayBuilder(f);

	private List<Node> buildNonClosedWay(OsmWay way,
			TLongObjectMap<OsmNode> nodes, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		WayBuilderResult build = wb.build(way, entityProvider);
		GeometryGroup group = build.toGeometryGroup(f);

		QueryUtil.putNodes(way, nodes, entityProvider);
		return tree.query(group);
	}

	private List<Node> buildClosedWay(OsmWay way,
			TLongObjectMap<OsmNode> nodes, OsmEntityProvider entityProvider)
			throws EntityNotFoundException
	{
		WayBuilderResult build = wb.build(way, entityProvider);
		GeometryGroup group = build.toGeometryGroup(f);
		LinearRing ring = build.getLinearRing();
		Polygon polygon = f.createPolygon(ring);
		QueryUtil.putNodes(way, nodes, entityProvider);

		List<Node> leafs1 = new ArrayList<>(tree.query(group));
		List<Node> leafs2 = new ArrayList<>(tree.query(polygon));
		if (leafs1.size() == 1 && leafs2.size() == 1
				&& leafs1.get(0) == leafs2.get(0)) {
			return leafs1;
		} else {
			List<Node> merged = merge(leafs1, leafs2);
			if (merged.size() > leafs1.size()) {
				System.out
						.println(String
								.format("found way that contains leafs. outline: %d polygon: %d merged: %d",
										leafs1.size(), leafs2.size(),
										merged.size()));
			}
			return merged;
		}
	}

	private List<Node> merge(List<Node> a, List<Node> b)
	{
		List<Node> result = new ArrayList<>();
		Set<Node> set = new HashSet<>();
		for (Node node : a) {
			result.add(node);
			set.add(node);
		}
		for (Node node : b) {
			if (!set.contains(node)) {
				result.add(node);
			}
		}
		return result;
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

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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.nodearray.NodeArray;
import de.topobyte.osm4j.extra.nodearray.NodeArrayInteger;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class WaysToTreeMapperUsingArray
{

	private OsmIterator wayIterator;

	private Path pathTree;
	private String fileNames;

	private Path pathNodeArray;

	private OsmOutputConfig outputConfig;

	public WaysToTreeMapperUsingArray(OsmIterator wayIterator, Path pathTree,
			String fileNames, Path pathNodeArray, OsmOutputConfig outputConfig)
	{
		this.wayIterator = wayIterator;
		this.pathTree = pathTree;
		this.fileNames = fileNames;
		this.pathNodeArray = pathNodeArray;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		/*
		 * Tree, node array and way iterator
		 */

		NodeArray array = new NodeArrayInteger(pathNodeArray.toFile(), 1024,
				4096);
		DataTree tree = DataTreeOpener.open(pathTree.toFile());

		// This is where we write ways to that do not contain any reference
		// within the world bounds

		Path pathNonMatched = pathTree.resolve("non-matched-ways.tbo");
		OutputStream bosNone = StreamUtil.bufferedOutputStream(pathNonMatched
				.toFile());
		OsmOutputStream osmOutputNone = OsmIoUtils.setupOsmOutput(bosNone,
				outputConfig);
		OsmStreamOutput outputNone = new OsmOutputStreamStreamOutput(bosNone,
				osmOutputNone);

		// Set up outputs

		ClosingFileOutputStreamFactory outputStreamFactory = new SimpleClosingFileOutputStreamFactory();

		Map<Node, OsmStreamOutput> outputs = new HashMap<>();

		for (Node leaf : tree.getLeafs()) {
			String dirname = Long.toHexString(leaf.getPath());
			Path dir = pathTree.resolve(dirname);
			Path file = dir.resolve(fileNames);

			OutputStream os = outputStreamFactory.create(file.toFile());
			OutputStream bos = new BufferedOutputStream(os);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(bos,
					outputConfig);
			OsmStreamOutput output = new OsmOutputStreamStreamOutput(bos,
					osmOutput);
			outputs.put(leaf, output);

			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}

		// Process ways

		int nNone = 0;
		int nMultiple = 0;

		NodeProgress progress = new NodeProgress();
		progress.printTimed(1000);

		while (wayIterator.hasNext()) {
			EntityContainer container = wayIterator.next();
			if (container.getType() != EntityType.Way) {
				continue;
			}
			OsmWay way = (OsmWay) container.getEntity();

			if (way.getNumberOfNodes() == 0) {
				continue;
			}

			progress.increment();

			List<Node> leafs = null;
			for (int i = 0; i < way.getNumberOfNodes(); i++) {
				long nodeId = way.getNodeId(i);
				OsmNode node = array.get(nodeId);
				leafs = tree.query(node.getLongitude(), node.getLatitude());
				if (!leafs.isEmpty()) {
					break;
				}
			}

			if (leafs.size() == 0) {
				outputNone.getOsmOutput().write(way);
				nNone++;
			}

			if (leafs.size() > 1) {
				nMultiple++;
			}

			for (Node leaf : leafs) {
				OsmStreamOutput output = outputs.get(leaf);
				output.getOsmOutput().write(way);
			}
		}

		progress.stop();

		System.out.println("none: " + nNone);
		System.out.println("multiple: " + nMultiple);

		array.close();

		outputNone.getOsmOutput().complete();
		outputNone.close();

		for (OsmStreamOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

}

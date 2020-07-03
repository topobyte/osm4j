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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.TLongLongMap;
import com.slimjars.dist.gnu.trove.map.TObjectLongMap;
import com.slimjars.dist.gnu.trove.map.hash.TObjectLongHashMap;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;
import de.topobyte.osm4j.core.model.iface.OsmBounds;

public class DataTreeUtil
{

	final static Logger logger = LoggerFactory.getLogger(DataTreeUtil.class);

	public static void writeTreeInfo(File dir, BBox bbox)
			throws FileNotFoundException
	{
		File file = new File(dir, DataTree.FILENAME_INFO);

		PrintWriter pw = new PrintWriter(file);
		pw.println(DataTree.PROPERTY_BBOX + ": " + BBoxString.create(bbox));
		pw.close();
	}

	public static DataTree openExistingTree(Path dirOutput) throws IOException
	{
		if (!Files.exists(dirOutput)) {
			throw new IOException("Output path does not exist");
		}
		if (!Files.isDirectory(dirOutput)) {
			throw new IOException("Output path is not a directory");
		}

		return DataTreeOpener.open(dirOutput.toFile());
	}

	public static DataTree initNewTree(Path dirOutput, OsmBounds bounds)
			throws IOException
	{
		Envelope envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());

		BBox bbox = new BBox(envelope);

		return initNewTree(dirOutput, bbox);
	}

	public static DataTree initNewTree(Path dirOutput, BBox bbox)
			throws IOException
	{
		if (!Files.exists(dirOutput)) {
			logger.info("Creating output directory");
			Files.createDirectories(dirOutput);
		}
		if (!Files.isDirectory(dirOutput)) {
			throw new IOException("Output path is not a directory");
		}
		if (dirOutput.toFile().list().length != 0) {
			throw new IOException("Output directory is not empty");
		}

		DataTreeUtil.writeTreeInfo(dirOutput.toFile(), bbox);

		return new DataTree(bbox.toEnvelope());
	}

	public static void mergeUnderfilledSiblings(DataTree tree, Node head,
			int maxNodes, TLongLongMap counters)
	{
		List<Node> inner = tree.getInner(head);
		List<Node> leafs = tree.getLeafs(head);

		logger.debug("Before merging underfilled siblings:");
		logger.debug("inner nodes: " + inner.size());
		logger.debug("leafs: " + leafs.size());

		TObjectLongMap<Node> counts = new TObjectLongHashMap<>();
		for (Node leaf : leafs) {
			long count = counters.get(leaf.getPath());
			counts.put(leaf, count);
		}

		List<Node> check = new ArrayList<>(inner);
		Collections.sort(check, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2)
			{
				return Integer.compare(o2.getLevel(), o1.getLevel());
			}
		});
		for (Node node : check) {
			if (!node.getLeft().isLeaf() || !node.getRight().isLeaf()) {
				continue;
			}
			long sum = counts.get(node.getLeft()) + counts.get(node.getRight());
			if (sum < maxNodes) {
				node.melt();
				counts.put(node, sum);
			}
		}

		logger.debug("After:");
		logger.debug("inner nodes: " + tree.getInner(head).size());
		logger.debug("leafs: " + tree.getLeafs(head).size());
	}

}

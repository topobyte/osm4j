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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;

public class DataTreeOpener
{

	public static DataTree open(File dir) throws IOException
	{
		File fileInfo = new File(dir, DataTree.FILENAME_INFO);
		if (!fileInfo.exists()) {
			throw new FileNotFoundException("info file not found: " + fileInfo);
		}

		BBox bbox = null;

		BufferedReader reader = new BufferedReader(new FileReader(fileInfo));
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			if (bbox == null && line.startsWith(DataTree.PROPERTY_BBOX + ":")) {
				String data = line.substring(
						DataTree.PROPERTY_BBOX.length() + 1).trim();
				bbox = BBoxString.parse(data).toBbox();
			}
		}
		reader.close();

		if (bbox == null) {
			throw new IOException("No bounding box found in info file");
		}

		Envelope envelope = bbox.toEnvelope();
		DataTree tree = new DataTree(envelope);

		// Find all data files by extension
		List<File> dataFiles = new ArrayList<>();

		File[] files = dir.listFiles();
		for (File file : files) {
			if (!file.isDirectory()) {
				continue;
			}
			String name = file.getName();
			try {
				Long.parseLong(name, 16);
				dataFiles.add(file);
			} catch (NumberFormatException e) {
				System.out.println("Warning: unknown directory: " + file);
			}
		}

		if (dataFiles.isEmpty()) {
			throw new IOException("No data available");
		}

		/*
		 * Rebuild the tree bottom up
		 */

		// This set contains all paths to nodes that have children. Using this
		// information we can rebuild our tree by starting with a tree with only
		// a root node and then recursively splitting each node in the tree that
		// has children.
		Set<Long> hasChildren = new HashSet<>();

		// First determine each node's level and setup a group of nodes for
		// each level with at least one node on it.
		Map<Integer, Set<Long>> layerMap = new HashMap<>();

		for (File file : dataFiles) {
			// Each directory represents a node
			String name = file.getName();
			// Decode the path
			long path = Long.parseLong(name, 16);
			// Determine the level from the path
			int level = Long.toBinaryString(path).length() - 1;

			// Put node into layer group
			Set<Long> layer = layerMap.get(level);
			if (layer == null) {
				layer = new HashSet<>();
				layerMap.put(level, layer);
			}
			layer.add(path);
		}

		// Build list of all available layers, sorted ascending by layer
		List<Integer> levels = new ArrayList<>(layerMap.keySet());
		Collections.sort(levels);

		// Go through layers, bottom to top
		int maxLevel = levels.get(levels.size() - 1);
		for (int level = maxLevel; level > 0; level--) {
			// Get a reference to the current layer
			Set<Long> layer = layerMap.get(level);
			// Get the layer above the current, create if necessary
			Set<Long> above = layerMap.get(level - 1);
			if (above == null) {
				above = new HashSet<>();
				layerMap.put(level - 1, above);
			}

			// As long as there are nodes on the layer, find siblings and create
			// a parent node on the layer above
			while (!layer.isEmpty()) {
				// Remove a node from the layer
				Iterator<Long> iterator = layer.iterator();
				long path = iterator.next();
				iterator.remove();

				// Determine its sibling node
				long sibling = sibling(path);
				boolean remove = layer.remove(sibling);
				if (!remove) {
					throw new IOException("Missing file for node: "
							+ Long.toHexString(sibling));
				}

				// Determine the parent's path
				long parent = parent(path);
				if (above.contains(parent)) {
					throw new IOException("Parent node shouldn't exists: "
							+ Long.toHexString(parent));
				}

				// Add the parent to the layer above
				above.add(parent);
				// And store that the parent node has children
				hasChildren.add(parent);
			}
		}

		// Rebuild tree, splitting using the previously built information
		Set<Node> work = new HashSet<>();
		work.add(tree.getRoot());

		while (!work.isEmpty()) {
			// Get any node that we need to work on
			Iterator<Node> iterator = work.iterator();
			Node node = iterator.next();
			iterator.remove();

			// Split if we previously determined that this node has children
			if (hasChildren.contains(node.getPath())) {
				node.split();
				// And recurse on the newly created nodes
				work.add(node.getLeft());
				work.add(node.getRight());
			}
		}

		return tree;
	}

	private static long parent(long path)
	{
		return path >> 1;
	}

	private static long sibling(long path)
	{
		long lastBit = path & 1;
		long siblingLastBit = (~lastBit) & 1;
		return (path & ~1) | siblingLastBit;
	}

}

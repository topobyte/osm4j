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

import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;

public class DataTreeUtil
{

	public static void writeTreeInfo(File dir, BBox bbox)
			throws FileNotFoundException
	{
		File file = new File(dir, DataTree.FILENAME_INFO);

		PrintWriter pw = new PrintWriter(file);
		pw.println(DataTree.PROPERTY_BBOX + ": " + BBoxString.create(bbox));
		pw.close();
	}

	public static void mergeUnderfilledSiblings(DataTree tree, Node head,
			int maxNodes, TLongLongMap counters)
	{
		List<Node> inner = tree.getInner(head);
		List<Node> leafs = tree.getLeafs(head);

		System.out.println("Before merging underfilled siblings:");
		System.out.println("inner nodes: " + inner.size());
		System.out.println("leafs: " + leafs.size());

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

		System.out.println("After:");
		System.out.println("inner nodes: " + tree.getInner(head).size());
		System.out.println("leafs: " + tree.getLeafs(head).size());
	}

}

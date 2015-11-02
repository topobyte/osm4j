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

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class DataTree
{

	public static final String FILENAME_INFO = "tree.info";
	public static final String PROPERTY_BBOX = "bbox";

	private Node root;

	public DataTree(Envelope envelope)
	{
		root = new Node(envelope, null, 1);
	}

	public Node getRoot()
	{
		return root;
	}

	public List<Node> getLeafs()
	{
		List<Node> leafs = new ArrayList<>();

		getLeafs(root, leafs);

		return leafs;
	}

	private void getLeafs(Node node, List<Node> leafs)
	{
		if (node.isLeaf()) {
			leafs.add(node);
		} else {
			getLeafs(node.getLeft(), leafs);
			getLeafs(node.getRight(), leafs);
		}
	}

	public void print()
	{
		print(root);
	}

	private void print(Node node)
	{
		if (node.isLeaf()) {
			System.out.println(Integer.toHexString(node.getPath()) + ": "
					+ node.getEnvelope());
		} else {
			print(node.getLeft());
			print(node.getRight());
		}
	}

	private List<Node> results = new ArrayList<>();

	public List<Node> query(double lon, double lat)
	{
		results.clear();
		if (root.getEnvelope().contains(lon, lat)) {
			root.query(results, lon, lat);
		}
		return results;
	}

}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;

public class TreeSplitter
{

	private DataTree tree;

	private Set<Node> nodes = new HashSet<>();
	private Map<Node, List<OsmNode>> bags = new HashMap<>();

	public TreeSplitter(DataTree tree)
	{
		this.tree = tree;
	}

	public void split(OsmIterator iterator, int maxNodes)
	{
		for (Node leaf : tree.getLeafs()) {
			init(leaf);
		}

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			double lon = node.getLongitude();
			double lat = node.getLatitude();

			List<Node> leafs = tree.query(lon, lat);
			for (Node leaf : leafs) {
				List<OsmNode> bag = bags.get(leaf);
				bag.add(node);
				if (bag.size() > maxNodes) {
					split(leaf);
				}
			}
		}
	}

	private void init(Node node)
	{
		nodes.add(node);
		bags.put(node, new ArrayList<OsmNode>());
	}

	private void split(Node leaf)
	{
		leaf.split();
		Node left = leaf.getLeft();
		Node right = leaf.getRight();

		init(left);
		init(right);

		nodes.remove(leaf);
		List<OsmNode> bag = bags.remove(leaf);

		for (OsmNode node : bag) {
			double lon = node.getLongitude();
			double lat = node.getLatitude();
			if (left.getEnvelope().contains(lon, lat)) {
				bags.get(left).add(node);
			}
			if (right.getEnvelope().contains(lon, lat)) {
				bags.get(right).add(node);
			}
		}
	}

}

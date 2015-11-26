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

package de.topobyte.osm4j.extra.datatree.nodetree.distribute;

import java.io.IOException;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
import de.topobyte.osm4j.extra.progress.NodeProgress;

public class SimpleNodeTreeDistributor extends AbstractNodeTreeDistributor
{

	private DataTreeOutputFactory outputFactory;

	public SimpleNodeTreeDistributor(DataTree tree, Node head,
			OsmIterator iterator, DataTreeOutputFactory outputFactory)
	{
		super(tree, head, iterator);
		this.outputFactory = outputFactory;
	}

	@Override
	protected void initOutputs() throws IOException
	{
		List<Node> leafs = tree.getLeafs(head);
		for (Node leaf : leafs) {
			OsmStreamOutput output = outputFactory.init(leaf, true);
			outputs.put(leaf, output);
		}
	}

	@Override
	protected void distributeNodes() throws IOException
	{
		NodeProgress counter = new NodeProgress();
		counter.printTimed(1000);

		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				OsmNode node = (OsmNode) entityContainer.getEntity();
				writeToLeafs(node);
				counter.increment();
				break;
			case Way:
				break loop;
			case Relation:
				break loop;
			}
		}
		counter.stop();
	}

	private void writeToLeafs(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(head, node.getLongitude(),
				node.getLatitude());
		for (Node leaf : leafs) {
			OsmStreamOutput output = outputs.get(leaf);
			output.getOsmOutput().write(node);
		}
	}

}

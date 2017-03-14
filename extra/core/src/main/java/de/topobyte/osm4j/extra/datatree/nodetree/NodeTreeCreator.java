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

package de.topobyte.osm4j.extra.datatree.nodetree;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributor;
import de.topobyte.osm4j.extra.datatree.nodetree.distribute.NodeTreeDistributorFactory;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;

public class NodeTreeCreator
{

	private DataTree tree;
	private OsmIterator iterator;
	private DataTreeOutputFactory outputFactory;
	private NodeTreeDistributorFactory distributorFactory;

	public NodeTreeCreator(DataTree tree, OsmIterator iterator,
			DataTreeOutputFactory outputFactory,
			NodeTreeDistributorFactory distributorFactory)
	{
		this.tree = tree;
		this.outputFactory = outputFactory;
		this.distributorFactory = distributorFactory;
		this.iterator = iterator;
	}

	public void execute() throws IOException
	{
		NodeTreeDistributor distributor = distributorFactory.createDistributor(
				tree, tree.getRoot(), iterator, outputFactory);

		distributor.execute();
	}

}

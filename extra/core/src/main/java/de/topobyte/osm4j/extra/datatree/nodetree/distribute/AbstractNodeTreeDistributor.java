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
import java.util.HashMap;
import java.util.Map;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public abstract class AbstractNodeTreeDistributor implements
		NodeTreeDistributor
{

	protected DataTree tree;
	protected OsmIterator iterator;
	protected OsmOutputConfig outputConfig;

	protected Node head;

	protected Map<Node, OsmStreamOutput> outputs = new HashMap<>();

	public AbstractNodeTreeDistributor(DataTree tree, Node head,
			OsmIterator iterator)
	{
		this.tree = tree;
		this.iterator = iterator;
		this.head = head;
	}

	public Node getHead()
	{
		return head;
	}

	public Map<Node, OsmStreamOutput> getOutputs()
	{
		return outputs;
	}

	@Override
	public void execute() throws IOException
	{
		initOutputs();

		distributeNodes();

		finish();
	}

	protected abstract void initOutputs() throws IOException;

	protected abstract void distributeNodes() throws IOException;

	private void finish() throws IOException
	{
		for (OsmStreamOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

}

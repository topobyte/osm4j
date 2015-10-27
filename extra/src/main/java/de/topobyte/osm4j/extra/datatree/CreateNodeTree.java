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

import java.io.IOException;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTree extends BaseNodeTreeCreator
{

	private static final String OPTION_SPLIT_DEPTH = "split_depth";

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTree.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTree task = new CreateNodeTree();

		task.setup(args);

		task.readMetadata = true;

		task.init();

		task.initOutputs();

		task.run();

		task.finish();
	}

	private int splitDepth;

	public CreateNodeTree()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_SPLIT_DEPTH, true, true, "how often to split the root node");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argSplitDepth = line.getOptionValue(OPTION_SPLIT_DEPTH);

		splitDepth = Integer.parseInt(argSplitDepth);
		if (splitDepth < 0) {
			System.out.println("Please specify a positive split depth");
			System.exit(1);
		}
	}

	protected void initOutputs() throws IOException
	{
		OsmBounds bounds = inputIterator.getBounds();
		Envelope envelope = new Envelope(bounds.getLeft(), bounds.getRight(),
				bounds.getBottom(), bounds.getTop());

		tree = new DataTree(envelope);
		tree.getRoot().split(splitDepth);

		System.out.println("bounds: " + bounds);
		tree.print();

		List<Node> leafs = tree.getLeafs();
		for (Node leaf : leafs) {
			init(leaf);
		}
	}

	@Override
	protected void handle(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			if (leaf.getEnvelope().contains(node.getLongitude(),
					node.getLatitude())) {
				Output output = outputs.get(leaf);
				output.getOsmOutput().write(node);
			}
		}
	}

}

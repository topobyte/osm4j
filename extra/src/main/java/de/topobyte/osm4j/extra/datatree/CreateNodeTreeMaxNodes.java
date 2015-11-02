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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeTreeMaxNodes extends BaseNodeTreeCreator
{

	private static final String OPTION_MAX_NODES = "max_nodes";

	private static final String OPTION_PRE_SPLIT_FORMAT = "pre_split_format";
	private static final String OPTION_PRE_SPLIT_DATA = "pre_split_data";
	private static final String OPTION_PRE_SPLIT_MAX = "pre_split_max";

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTreeMaxNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTreeMaxNodes task = new CreateNodeTreeMaxNodes();

		task.setup(args);

		task.readMetadata = true;

		task.init();

		task.initTree();

		task.initOutputs();

		task.run();

		task.finish();
	}

	private int maxNodes;

	private int preSplitMaxNodes;
	private FileFormat preSplitFormat;
	private String preSplitPath;

	public CreateNodeTreeMaxNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_MAX_NODES, true, true, "the maximum number of nodes per file");

		OptionHelper.add(options, OPTION_PRE_SPLIT_FORMAT, true, false, "file format of pre split data");
		OptionHelper.add(options, OPTION_PRE_SPLIT_DATA, true, false, "path to pre split data");
		OptionHelper.add(options, OPTION_PRE_SPLIT_MAX, true, false, "max nodes per file pre split");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argMaxNodes = line.getOptionValue(OPTION_MAX_NODES);

		maxNodes = Integer.parseInt(argMaxNodes);
		if (maxNodes < 1) {
			System.out.println("Please specify a max nodes >= 1");
			System.exit(1);
		}

		boolean hasPreSplitFormat = line.hasOption(OPTION_PRE_SPLIT_FORMAT);
		boolean hasPreSplitData = line.hasOption(OPTION_PRE_SPLIT_DATA);
		boolean hasPreSplitMax = line.hasOption(OPTION_PRE_SPLIT_MAX);

		if (hasPreSplitData || hasPreSplitFormat || hasPreSplitMax) {
			if (!hasPreSplitData || !hasPreSplitFormat || !hasPreSplitMax) {
				System.out
						.println("Please specifiy all or none options for pre splitting");
				System.exit(1);
			}

			preSplitMaxNodes = Integer.parseInt(line
					.getOptionValue(OPTION_PRE_SPLIT_MAX));
			if (preSplitMaxNodes < 1) {
				System.out.println("Please specify a pre split max nodes >= 1");
				System.exit(1);
			}

			String preSplitFormatName = line
					.getOptionValue(OPTION_PRE_SPLIT_FORMAT);
			preSplitFormat = FileFormat.parseFileFormat(preSplitFormatName);
			if (outputFormat == null) {
				System.out.println("invalid pre split format");
				System.out.println("please specify one of: "
						+ FileFormat.getHumanReadableListOfSupportedFormats());
				System.exit(1);
			}

			preSplitPath = line.getOptionValue(OPTION_PRE_SPLIT_DATA);
		}
	}

	@Override
	protected void initTree() throws IOException
	{
		super.initTree();

		if (preSplitPath != null) {
			System.out.println("Splitting tree with warm up data");
			TreeSplitter splitter = new TreeSplitter(tree);
			InputStream input = new FileInputStream(preSplitPath);
			OsmIterator iterator = Util.setupOsmInput(input, preSplitFormat,
					false);
			splitter.split(iterator, preSplitMaxNodes);
			System.out.println("Number of leafs: " + tree.getLeafs().size());
		}

		tree.print();
	}

	protected void initOutputs() throws IOException
	{
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
				output.incrementCounter();
				output.getOsmOutput().write(node);
				if (output.getCount() > maxNodes) {
					split(leaf);
				}
			}
		}
	}

	private void split(Node leaf) throws IOException
	{
		leaf.split();
		Node left = leaf.getLeft();
		Node right = leaf.getRight();

		System.out.println("Splitting: " + leaf.getEnvelope());
		System.out.println(String.format("%s -> %s %s",
				Integer.toHexString(leaf.getPath()),
				Integer.toHexString(left.getPath()),
				Integer.toHexString(right.getPath())));

		Output output = outputs.get(leaf);
		output.getOsmOutput().complete();
		output.getOutputStream().close();
		outputs.remove(leaf);

		Output outLeft = init(left);
		Output outRight = init(right);

		InputStream input = new FileInputStream(output.getFile());
		input = new BufferedInputStream(input);
		OsmIterator iterator = Util.setupOsmInput(input, outputFormat,
				writeMetadata);
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				continue;
			}
			OsmNode node = (OsmNode) container.getEntity();
			double lon = node.getLongitude();
			double lat = node.getLatitude();
			Side side = leaf.side(lon, lat);
			switch (side) {
			case ON:
				outLeft.incrementCounter();
				outLeft.getOsmOutput().write(node);
				outRight.incrementCounter();
				outRight.getOsmOutput().write(node);
				break;
			case LEFT:
				outLeft.incrementCounter();
				outLeft.getOsmOutput().write(node);
				break;
			case RIGHT:
				outRight.incrementCounter();
				outRight.getOsmOutput().write(node);
				break;
			}
		}

		System.out.println("Deleting " + Integer.toHexString(leaf.getPath()));
		output.getFile().delete();
		output.getFile().getParentFile().delete();
	}

}

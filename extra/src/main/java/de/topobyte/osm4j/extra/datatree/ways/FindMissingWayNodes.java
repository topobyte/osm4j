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

package de.topobyte.osm4j.extra.datatree.ways;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.utils.AbstractTask;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class FindMissingWayNodes extends AbstractTask
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_INPUT_FORMAT = "input_format";
	private static final String OPTION_FILE_NAMES_NODES = "nodes";
	private static final String OPTION_FILE_NAMES_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return FindMissingWayNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		FindMissingWayNodes task = new FindMissingWayNodes();

		task.setup(args);

		task.execute();
	}

	private String pathNodeTree;
	private String pathWayTree;
	private String pathOutputTree;

	private String fileNamesNodes;
	private String fileNamesWays;
	private String fileNamesOutput;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	public FindMissingWayNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_FILE_NAMES_NODES, true, true, "names of the node files in the tree");
		OptionHelper.add(options, OPTION_FILE_NAMES_WAYS, true, true, "names of the way files in the tree");
		OptionHelper.add(options, OPTION_INPUT_FORMAT, true, true, "the file format of the input");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		PbfOptions.add(options);
		TboOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String inputFormatName = line.getOptionValue(OPTION_INPUT_FORMAT);
		FileFormat inputFormat = FileFormat.parseFileFormat(inputFormatName);
		if (inputFormat == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}
		inputFormatNodes = inputFormat;
		inputFormatWays = inputFormat;

		fileNamesNodes = line.getOptionValue(OPTION_FILE_NAMES_NODES);
		fileNamesWays = line.getOptionValue(OPTION_FILE_NAMES_WAYS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);

		String pathTree = line.getOptionValue(OPTION_TREE);
		pathNodeTree = pathTree;
		pathWayTree = pathTree;
		pathOutputTree = pathTree;
	}

	private List<Node> leafs;

	private long counter = 0;
	private long found = 0;
	private long notFound = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	public void execute() throws IOException
	{
		DataTree tree = DataTreeOpener.open(new File(pathNodeTree));

		File dirNodeTree = new File(pathNodeTree);
		File dirWayTree = new File(pathWayTree);
		File dirOutputTree = new File(pathOutputTree);

		DataTreeFiles filesNodes = new DataTreeFiles(dirNodeTree,
				fileNamesNodes);
		DataTreeFiles filesWays = new DataTreeFiles(dirWayTree, fileNamesWays);
		DataTreeFiles filesOutput = new DataTreeFiles(dirOutputTree,
				fileNamesOutput);

		leafs = tree.getLeafs();

		int i = 0;
		for (Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			File fileNodes = filesNodes.getFile(leaf);
			File fileWays = filesWays.getFile(leaf);
			File fileOutput = filesOutput.getFile(leaf);

			InputStream inputNodes = new BufferedInputStream(
					new FileInputStream(fileNodes));
			InputStream inputWays = new BufferedInputStream(
					new FileInputStream(fileWays));

			long nodesSize = fileNodes.length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize / 1024. / 1024.));

			OsmIdIterator idIterator = OsmIoUtils.setupOsmIdIterator(
					inputNodes, inputFormatNodes);
			TLongSet nodeIds = read(idIterator);

			long waysSize = fileWays.length();
			System.out.println(String.format(
					"Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));

			InMemoryDataSet dataWays = DataSetReader.read(OsmIoUtils
					.setupOsmIterator(inputWays, inputFormatWays, false),
					false, false, false);

			inputNodes.close();
			inputWays.close();

			System.out.println("Number of ways: " + dataWays.getWays().size());

			TLongSet missingIds = new TLongHashSet();

			TLongObjectIterator<OsmWay> ways = dataWays.getWays().iterator();
			while (ways.hasNext()) {
				ways.advance();
				OsmWay way = ways.value();
				build(way, nodeIds, missingIds);
			}

			System.out.println("Sorting id list of size: " + missingIds.size());

			TLongList missingIdList = new TLongArrayList(missingIds);
			missingIdList.sort();

			System.out.println("Writing missing ids");
			OutputStream fos = new FileOutputStream(fileOutput);
			OutputStream bos = new BufferedOutputStream(fos);
			IdListOutputStream idOutput = new IdListOutputStream(bos);
			TLongIterator iterator = missingIdList.iterator();
			while (iterator.hasNext()) {
				idOutput.write(iterator.next());
			}
			idOutput.close();

			stats(i);
		}
	}

	private TLongSet read(OsmIdIterator idIterator)
	{
		TLongSet ids = new TLongHashSet();
		while (idIterator.hasNext()) {
			IdContainer container = idIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			ids.add(container.getId());
		}
		return ids;
	}

	private void build(OsmWay way, TLongSet nodeIds, TLongSet missing)
			throws IOException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			if (nodeIds.contains(nodeId)) {
				found++;
			} else {
				notFound++;
				missing.add(nodeId);
			}
		}
		counter++;
	}

	private void stats(int leafsDone)
	{
		double ratio = notFound / (double) (found + notFound);
		System.out.println(String.format(
				"ways: %s, found ids: %s, missing ids: %s, ratio: %f",
				format.format(counter), format.format(found),
				format.format(notFound), ratio));

		long now = System.currentTimeMillis();
		long past = now - start;
		long estimate = Math.round((past / (double) leafsDone) * leafs.size());
		System.out.println(String.format("Past: %.2f", past / 1000 / 60.));
		System.out.println(String.format("Estimate: %.2f",
				estimate / 1000 / 60.));
	}

}

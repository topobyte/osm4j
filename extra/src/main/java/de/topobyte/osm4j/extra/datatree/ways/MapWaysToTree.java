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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.topobyte.largescalefileio.ClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.extra.ways.WayNodeIdComparator;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputFile;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.osm4j.utils.merge.sorted.SortedMergeIterator;
import de.topobyte.osm4j.utils.sort.IdComparator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class MapWaysToTree extends AbstractTaskSingleInputFile
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_WAYS = "ways";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";

	@Override
	protected String getHelpMessage()
	{
		return MapWaysToTree.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		MapWaysToTree task = new MapWaysToTree();

		task.setup(args);

		task.prepare();

		task.execute();
	}

	private String pathTree;
	private String pathWays;

	private String fileNamesOutput;

	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata = true;

	public MapWaysToTree()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_WAYS, true, true, "directory with ways sorted by first node id");
		OptionHelper.add(options, OPTION_TREE, true, true, "tree directory to work on");
		PbfOptions.add(options);
		TboOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String outputFormatName = line.getOptionValue(OPTION_OUTPUT_FORMAT);
		outputFormat = FileFormat.parseFileFormat(outputFormatName);
		if (outputFormat == null) {
			System.out.println("invalid output format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pbfConfig = PbfOptions.parse(line);
		tboConfig = TboOptions.parse(line);

		pathWays = line.getOptionValue(OPTION_WAYS);
		pathTree = line.getOptionValue(OPTION_TREE);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);
	}

	private DataTree tree;
	private OsmIterator nodeIterator;
	private SortedMergeIterator wayIterator;

	private Map<Node, Output> outputs = new HashMap<>();
	private List<InputStream> wayInputStreams = new ArrayList<>();

	public void prepare() throws IOException
	{
		tree = DataTreeOpener.open(new File(pathTree));

		File dirTree = new File(pathTree);
		File dirWays = new File(pathWays);

		DataTreeFiles filesOutput = new DataTreeFiles(dirTree, fileNamesOutput);

		List<Node> leafs = tree.getLeafs();

		// Node input
		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		nodeIterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);

		// Node outputs
		ClosingFileOutputStreamFactory factoryOut = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : leafs) {
			File fileOutput = filesOutput.getFile(leaf);
			OutputStream output = factoryOut.create(fileOutput);
			output = new BufferedOutputStream(output);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputFormat, writeMetadata, pbfConfig, tboConfig);

			Output out = new Output(fileOutput.toPath(), output, osmOutput);
			outputs.put(leaf, out);
		}

		// Way inputs
		ClosingFileInputStreamFactory factoryIn = new SimpleClosingFileInputStreamFactory();

		List<OsmIterator> wayIterators = new ArrayList<>();
		File[] wayFiles = dirWays.listFiles();
		for (File file : wayFiles) {
			InputStream inputWays = factoryIn.create(file);
			inputWays = new BufferedInputStream(inputWays);
			wayInputStreams.add(inputWays);
			OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(inputWays,
					inputFormat, writeMetadata);
			wayIterators.add(osmIterator);
		}

		wayIterator = new SortedMergeIterator(wayIterators, new IdComparator(),
				new WayNodeIdComparator(), new IdComparator());
	}

	private NodeProgress progress = new NodeProgress();
	private OsmWay way = null;
	private long next = -1;

	private boolean advanceWay()
	{
		while (wayIterator.hasNext()) {
			EntityContainer c = wayIterator.next();
			if (c.getType() != EntityType.Way) {
				continue;
			}

			way = (OsmWay) c.getEntity();
			next = way.getNodeId(0);
			return true;
		}
		way = null;
		next = -1;
		return false;
	}

	public void execute() throws IOException
	{
		progress.printTimed(1000);

		advanceWay();

		while (nodeIterator.hasNext()) {
			EntityContainer container = nodeIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			long id = node.getId();
			progress.increment();

			if (next > id) {
				// We don't need this node
				continue;
			} else if (next == id) {
				// We need this node
				query(node);
				// Could be that more outputs are waiting for this node
				while (advanceWay() && next == id) {
					query(node);
				}
			} else {
				// Some node that we are waiting for is not available on the
				// node input source
				while (advanceWay() && next < id) {
					query(node);
				}
			}
			if (way == null) {
				break;
			}
		}

		progress.stop();

		for (InputStream input : wayInputStreams) {
			input.close();
		}

		for (Output output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

	private void query(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			Output output = outputs.get(leaf);
			output.getOsmOutput().write(way);
		}
	}

}

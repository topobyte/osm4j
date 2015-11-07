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
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.topobyte.largescalefileio.ClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idlist.IdListInputStream;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputFile;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractMissingWayNodes extends AbstractTaskSingleInputFile
{

	private static final String OPTION_TREE = "tree";
	private static final String OPTION_FILE_NAMES_IDS = "ids";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";

	@Override
	protected String getHelpMessage()
	{
		return ExtractMissingWayNodes.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractMissingWayNodes task = new ExtractMissingWayNodes();

		task.setup(args);

		task.prepare();

		task.execute();
	}

	private String pathIdTree;
	private String pathOutputTree;

	private String fileNamesIds;
	private String fileNamesOutput;

	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata = true;

	public ExtractMissingWayNodes()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_FILE_NAMES_IDS, true, true, "names of the node id files in the tree");
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

		fileNamesIds = line.getOptionValue(OPTION_FILE_NAMES_IDS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);

		String pathTree = line.getOptionValue(OPTION_TREE);
		pathIdTree = pathTree;
		pathOutputTree = pathTree;
	}

	private List<Node> leafs;
	private OsmIterator iterator;
	private Map<Node, Output> outputs;
	private PriorityQueue<IdInput> queue;

	public void prepare() throws IOException
	{
		DataTree tree = DataTreeOpener.open(new File(pathIdTree));

		File dirIdTree = new File(pathIdTree);
		File dirOutputTree = new File(pathOutputTree);

		DataTreeFiles filesIds = new DataTreeFiles(dirIdTree, fileNamesIds);
		DataTreeFiles filesOutput = new DataTreeFiles(dirOutputTree,
				fileNamesOutput);

		leafs = tree.getLeafs();

		// Node input
		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);

		outputs = new HashMap<>();

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

		queue = new PriorityQueue<>(leafs.size(), new IdInputComparator());

		// Id inputs
		ClosingFileInputStreamFactory factoryIn = new SimpleClosingFileInputStreamFactory();

		for (Node leaf : leafs) {
			File fileIds = filesIds.getFile(leaf);
			InputStream inputIds = factoryIn.create(fileIds);
			inputIds = new BufferedInputStream(inputIds);
			IdListInputStream idInput = new IdListInputStream(inputIds);

			try {
				IdInput mergeInput = new IdInput(leaf, idInput);
				queue.add(mergeInput);
			} catch (EOFException e) {
				continue;
			}
		}
	}

	public void execute() throws IOException
	{
		NodeProgress progress = new NodeProgress();
		progress.printTimed(1000);

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			progress.increment();

			if (queue.isEmpty()) {
				break;
			}

			long id = node.getId();

			IdInput input = queue.peek();
			long next = input.getNext();

			if (next > id) {
				// We don't need this node
				continue;
			} else if (next == id) {
				// We need this node
				write(queue.poll(), node);
				// Could be that more outputs are waiting for this node
				while (!queue.isEmpty() && queue.peek().getNext() == id) {
					write(queue.poll(), node);
				}
			} else {
				// Some node that we are waiting for is not available on the
				// node input source
				skip(queue.poll());
				while (!queue.isEmpty() && queue.peek().getNext() < id) {
					skip(queue.poll());
				}
			}
		}

		progress.stop();

		for (Output output : outputs.values()) {
			output.getOsmOutput().complete();
			output.getOutputStream().close();
		}
	}

	private void write(IdInput input, OsmNode node) throws IOException
	{
		Output output = outputs.get(input.getNode());
		output.getOsmOutput().write(node);
		try {
			input.next();
			queue.add(input);
		} catch (EOFException e) {
			input.close();
		}
	}

	private void skip(IdInput input) throws IOException
	{
		try {
			input.next();
			queue.add(input);
		} catch (EOFException e) {
			input.close();
		}
	}

}

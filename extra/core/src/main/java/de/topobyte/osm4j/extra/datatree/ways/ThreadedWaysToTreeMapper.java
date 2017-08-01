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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.threading.ObjectBuffer;
import de.topobyte.osm4j.extra.threading.TaskRunnable;
import de.topobyte.osm4j.extra.threading.write.WayWriteRequest;
import de.topobyte.osm4j.extra.threading.write.WriteRequest;
import de.topobyte.osm4j.extra.threading.write.WriterRunner;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;

public class ThreadedWaysToTreeMapper implements WaysToTreeMapper
{

	private Path pathTree;
	private String fileNamesOutput;
	private OsmOutputConfig outputConfig;

	private AbstractWaysToTreeMapper mapper;

	public ThreadedWaysToTreeMapper(OsmIterator nodeIterator, Path pathTree,
			Path pathWays, FileFormat inputFormatWays, String fileNamesOutput,
			OsmOutputConfig outputConfig)
	{
		this.pathTree = pathTree;
		this.fileNamesOutput = fileNamesOutput;
		this.outputConfig = outputConfig;

		mapper = new AbstractWaysToTreeMapper(nodeIterator, pathTree, pathWays,
				inputFormatWays, outputConfig.isWriteMetadata()) {

			@Override
			protected void process(OsmWay way, Node leaf) throws IOException
			{
				put(way, leaf);
			}

			@Override
			protected void finish() throws IOException
			{
				super.finish();
				buffer.close();
			}

		};
	}

	private ObjectBuffer<WriteRequest> buffer = new ObjectBuffer<>(10000, 100);

	@Override
	public void execute() throws IOException
	{
		prepare();

		Runnable runnableMapper = new TaskRunnable(mapper);
		WriterRunner writer = new WriterRunner(buffer);

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(runnableMapper);
		tasks.add(writer);

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();

		finish();
	}

	protected void put(OsmWay way, Node leaf) throws IOException
	{
		OsmStreamOutput output = outputs.get(leaf.getPath());
		buffer.write(new WayWriteRequest(way, output.getOsmOutput()));
	}

	private DataTree tree;

	private TLongObjectMap<OsmStreamOutput> outputs = new TLongObjectHashMap<>();

	private void prepare() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());

		DataTreeFiles filesOutput = new DataTreeFiles(pathTree, fileNamesOutput);

		List<Node> leafs = tree.getLeafs();

		// Way outputs
		ClosingFileOutputStreamFactory factoryOut = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : leafs) {
			File fileOutput = filesOutput.getFile(leaf);
			OutputStream output = factoryOut.create(fileOutput);
			output = new BufferedOutputStream(output);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputConfig, true);

			OsmStreamOutput out = new OsmOutputStreamStreamOutput(output,
					osmOutput);
			outputs.put(leaf.getPath(), out);
		}
	}

	private void finish() throws IOException
	{
		for (OsmStreamOutput output : outputs.valueCollection()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

}

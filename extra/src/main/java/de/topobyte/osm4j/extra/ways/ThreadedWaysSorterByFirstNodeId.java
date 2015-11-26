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

package de.topobyte.osm4j.extra.ways;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.threading.Buffer;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;

public class ThreadedWaysSorterByFirstNodeId
{

	private OsmIterator input;
	private Path dirOutput;

	private OsmOutputConfig outputConfig;

	private Buffer<WayBatch> buffer = new Buffer<>(1);

	public ThreadedWaysSorterByFirstNodeId(OsmIterator input, Path dirOutput,
			OsmOutputConfig outputConfig)
	{
		this.input = input;
		this.dirOutput = dirOutput;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		init();

		RunnableWayBatchBuilder batchBuilder = new RunnableWayBatchBuilder(
				input, 800 * 1000, 10 * 1000 * 1000, buffer);

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(batchBuilder);
		tasks.add(sorterWriter);

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();
	}

	private void init() throws IOException
	{
		if (!Files.exists(dirOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(dirOutput);
		}
		if (!Files.isDirectory(dirOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (dirOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}
	}

	Runnable sorterWriter = new Runnable() {

		@Override
		public void run()
		{
			try {
				ThreadedWaysSorterByFirstNodeId.this.run();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private void run() throws IOException
	{
		for (WayBatch batch : buffer) {
			process(batch);
			status();
			System.out.println("returning object");
			buffer.returnObject(batch);
			System.out.println("done returning object");
		}
	}

	private int batchCount = 0;
	private int wayCount = 0;

	private long start = System.currentTimeMillis();
	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	private void status()
	{
		long now = System.currentTimeMillis();
		long past = now - start;

		double seconds = past / 1000;
		long perSecond = Math.round(wayCount / seconds);

		System.out.println(String.format(
				"Processed: %s ways, time passed: %.2f per second: %s",
				format.format(wayCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(WayBatch batch) throws IOException
	{
		List<OsmWay> ways = batch.getElements();
		System.out.println("processing batch with " + ways.size());

		Collections.sort(ways, new WayNodeIdComparator());
		System.out.println("sorting done");

		batchCount++;

		String filename = String.format("%d%s", batchCount,
				OsmIoUtils.extension(outputConfig.getFileFormat()));
		Path path = dirOutput.resolve(filename);
		File file = path.toFile();
		OutputStream output = StreamUtil.bufferedOutputStream(file);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputConfig);

		for (OsmWay way : ways) {
			osmOutput.write(way);
		}

		osmOutput.complete();
		output.close();
		System.out.println("writing done");

		wayCount += ways.size();
	}

}

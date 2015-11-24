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

package de.topobyte.osm4j.utils.split;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.buffer.OsmBuffer;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;
import de.topobyte.osm4j.utils.buffer.RunnableBufferBridge;

public class ThreadedEntitySplitter extends AbstractEntitySplitter
{

	private int bufferSize;
	private int maxNumberOfBuffers;

	public ThreadedEntitySplitter(OsmIterator iterator, Path pathNodes,
			Path pathWays, Path pathRelations, OsmOutputConfig outputConfig,
			int bufferSize, int maxNumberOfBuffers)
	{
		super(iterator, pathNodes, pathWays, pathRelations, outputConfig);
		this.bufferSize = bufferSize;
		this.maxNumberOfBuffers = maxNumberOfBuffers;
	}

	public void execute() throws IOException
	{
		init();
		passBounds();
		run();
		finish();
	}

	private void run() throws IOException
	{
		OsmBuffer buffer = new OsmBuffer(bufferSize, maxNumberOfBuffers);

		RunnableBufferBridge bridge = new RunnableBufferBridge(iterator, buffer);

		RunnableEntitySplitter splitter = new RunnableEntitySplitter(buffer,
				oosNodes, oosWays, oosRelations);

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(bridge);
		tasks.add(splitter);

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();
	}

}

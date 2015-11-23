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
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.buffer.OsmBuffer;
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

	private Throwable exceptionFromThread = null;

	private void run() throws IOException
	{
		final OsmBuffer buffer = new OsmBuffer(bufferSize, maxNumberOfBuffers);

		final RunnableBufferBridge bridge = new RunnableBufferBridge(iterator,
				buffer);

		final RunnableEntitySplitter splitter = new RunnableEntitySplitter(
				buffer, oosNodes, oosWays, oosRelations);

		final Thread readerThread = new Thread(bridge);
		final Thread splitterThread = new Thread(splitter);

		readerThread
				.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e)
					{
						bridge.stop();
						splitterThread.interrupt();
						exceptionFromThread = e;
					}
				});

		splitterThread
				.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e)
					{
						bridge.stop();
						readerThread.interrupt();
						exceptionFromThread = e;
					}
				});

		readerThread.start();
		splitterThread.start();

		while (true) {
			try {
				readerThread.join();
				splitterThread.join();
				break;
			} catch (InterruptedException e) {
				// continue
			}
		}

		if (exceptionFromThread != null) {
			Throwable cause = exceptionFromThread.getCause();
			if (cause instanceof IOException) {
				throw ((IOException) cause);
			}
			throw new RuntimeException(exceptionFromThread);
		}
	}

}

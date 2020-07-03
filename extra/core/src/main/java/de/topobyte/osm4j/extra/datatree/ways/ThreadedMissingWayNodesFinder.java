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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;

public class ThreadedMissingWayNodesFinder extends
		AbstractMissingWayNodesFinder
{

	public ThreadedMissingWayNodesFinder(Path pathNodeTree, Path pathWayTree,
			Path pathOutputTree, String fileNamesNodes, String fileNamesWays,
			String fileNamesOutput, FileFormat inputFormatNodes,
			FileFormat inputFormatWays)
	{
		super(pathNodeTree, pathWayTree, pathOutputTree, fileNamesNodes,
				fileNamesWays, fileNamesOutput, inputFormatNodes,
				inputFormatWays);
	}

	@Override
	public void execute() throws IOException
	{
		prepare();
		processLeafs();
	}

	public void processLeafs() throws IOException
	{
		BlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(10);
		ThreadPoolExecutor exec = new ThreadPoolExecutor(3, 3, 1,
				TimeUnit.MINUTES, tasks,
				new ThreadPoolExecutor.CallerRunsPolicy());

		int i = 0;
		for (final Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			final MissingWayNodesFinderTask task = creatTask(leaf);

			Runnable runnable = new Runnable() {

				@Override
				public void run()
				{
					try {
						task.execute();
						syncStats(task);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			};

			exec.execute(runnable);
		}

		exec.shutdown();

		while (!exec.isTerminated()) {
			try {
				exec.awaitTermination(1, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				// continue waiting
			}
		}
	}

	private synchronized void syncStats(MissingWayNodesFinderTask t)
	{
		stats(t);
	}

}

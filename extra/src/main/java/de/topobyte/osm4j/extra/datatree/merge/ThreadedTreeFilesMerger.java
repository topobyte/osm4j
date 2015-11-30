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

package de.topobyte.osm4j.extra.datatree.merge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class ThreadedTreeFilesMerger extends AbstractTreeFilesMerger
{

	public ThreadedTreeFilesMerger(Path pathTree, List<String> fileNamesSorted,
			List<String> fileNamesUnsorted, String fileNamesOutput,
			FileFormat inputFormat, OsmOutputConfig outputConfig,
			boolean deleteInput)
	{
		super(pathTree, fileNamesSorted, fileNamesUnsorted, fileNamesOutput,
				inputFormat, outputConfig, deleteInput);
	}

	@Override
	public void execute() throws IOException
	{
		prepare();

		run();
	}

	public void run() throws IOException
	{
		BlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(10);
		ThreadPoolExecutor exec = new ThreadPoolExecutor(3, 3, 1,
				TimeUnit.MINUTES, tasks,
				new ThreadPoolExecutor.CallerRunsPolicy());

		int i = 0;
		for (final Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			Runnable runnable = new Runnable() {

				@Override
				public void run()
				{
					try {
						mergeFiles(leaf);
						syncStats();
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

	private int leafsDone = 0;

	private synchronized void syncStats()
	{
		leafsDone++;
		stats(leafsDone);
	}

}

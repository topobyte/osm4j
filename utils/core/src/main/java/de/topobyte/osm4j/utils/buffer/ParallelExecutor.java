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

package de.topobyte.osm4j.utils.buffer;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParallelExecutor
{

	private Collection<Runnable> tasks;

	private Throwable exceptionFromThread = null;
	private List<StoppableRunnable> stoppables = new ArrayList<>();
	private List<Thread> threads = new ArrayList<>();

	public ParallelExecutor(Collection<Runnable> tasks)
	{
		this.tasks = tasks;
	}

	public void execute() throws IOException
	{
		for (Runnable task : tasks) {
			threads.add(new Thread(task));
			if (task instanceof StoppableRunnable) {
				stoppables.add((StoppableRunnable) task);
			}
		}

		for (Thread thread : threads) {
			thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e)
				{
					for (StoppableRunnable stoppable : stoppables) {
						stoppable.stop();
					}
					for (Thread thread : threads) {
						thread.interrupt();
					}
					exceptionFromThread = e;
				}
			});
		}

		for (Thread thread : threads) {
			thread.start();
		}

		while (true) {
			try {
				for (Thread thread : threads) {
					thread.join();
				}
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

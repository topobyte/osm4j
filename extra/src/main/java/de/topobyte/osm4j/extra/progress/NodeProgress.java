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

package de.topobyte.osm4j.extra.progress;

import java.text.NumberFormat;
import java.util.Locale;

public class NodeProgress
{

	private int num = 0;

	public void increment()
	{
		num++;
	}

	public void increment(int n)
	{
		num += n;
	}

	public void print(long time)
	{
		double seconds = time / 1000;
		long rSeconds = Math.round(seconds);
		long perSecond = Math.round(num / seconds);
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		format.setGroupingUsed(true);
		System.out.println(String.format("%ds: processed: %s per second: %s",
				rSeconds, format.format(num), format.format(perSecond)));
	}

	private long start;
	private long last;

	private boolean running = true;

	public void printTimed(final long interval)
	{
		start = System.currentTimeMillis();
		last = start;
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				while (running) {
					long current = System.currentTimeMillis();
					long past = current - last;
					if (past >= interval) {
						print(current - start);
						last = current;
					}
					long remaining = interval - (current - last);
					try {
						Thread.sleep(remaining);
					} catch (InterruptedException e) {
						// continue;
					}
				}
			}
		}).start();
	}

	public void stop()
	{
		running = false;
	}

}

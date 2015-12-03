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

package de.topobyte.osm4j.extra.extracts;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TimeTable
{

	private Map<String, Long> starts = new HashMap<String, Long>();
	private Map<String, Long> stops = new HashMap<String, Long>();

	public void start(String key)
	{
		starts.put(key, System.currentTimeMillis());
	}

	public void stop(String key)
	{
		stops.put(key, System.currentTimeMillis());
	}

	public long time(String key)
	{
		if (!starts.containsKey(key)) {
			return 0;
		}
		long stop;
		if (stops.containsKey(key)) {
			stop = stops.get(key);
		} else {
			stop = System.currentTimeMillis();
		}
		return stop - starts.get(key);
	}

	private PeriodFormatter formatter = new PeriodFormatterBuilder()
			.appendHours().appendSuffix("h").appendSeparator(" ")
			.appendMinutes().appendSuffix("m").appendSeparator(" ")
			.printZeroAlways().appendSeconds().appendSuffix("s").toFormatter();

	public String htime(String key)
	{
		long millis = time(key);
		Duration duration = new Duration(millis);
		Period period = duration.toPeriod();

		return formatter.print(period);
	}

}
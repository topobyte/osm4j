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

package de.topobyte.osm4j.extra.nodearray.coding;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.topobyte.osm4j.extra.nodearray.util.Coder;
import de.topobyte.osm4j.extra.nodearray.util.Coders;
import de.topobyte.osm4j.extra.nodearray.util.Interval;
import de.topobyte.osm4j.extra.nodearray.util.Intervals;

public class TestCoding
{

	@Test
	public void test()
	{
		Map<String, Coder<?>> nameToCoder = new HashMap<>();
		nameToCoder.put("int lon", Coders.CODER_INT_LON);
		nameToCoder.put("int lat", Coders.CODER_INT_LAT);
		nameToCoder.put("short lon", Coders.CODER_SHORT_LON);
		nameToCoder.put("short lat", Coders.CODER_SHORT_LAT);

		Map<String, Double> deltas = new HashMap<>();
		deltas.put("int lon", 0.00000005);
		deltas.put("int lat", 0.000000025);
		deltas.put("short lon", 0.003);
		deltas.put("short lat", 0.002);

		int steps = 10000;
		int random = 10000;

		for (String name : nameToCoder.keySet()) {
			Coder<? extends Number> coder = nameToCoder.get(name);
			double delta = deltas.get(name);

			Interval interval = coder.interval();

			double step = interval.getSize() / steps;
			for (int i = 0; i <= steps; i++) {
				double value = interval.getMin() + step * i;
				double decoded = endecode(coder, value);
				assertEquals(decoded, value, delta);
			}
		}

		for (String name : nameToCoder.keySet()) {
			Coder<? extends Number> coder = nameToCoder.get(name);
			double delta = deltas.get(name);

			Interval interval = coder.interval();

			for (int i = 0; i <= random; i++) {
				double value = Intervals.random(interval);
				double decoded = endecode(coder, value);
				assertEquals(decoded, value, delta);
			}
		}
	}

	private static <T extends Number> double endecode(Coder<T> coder,
			double value)
	{
		T encoded = coder.encode(value);
		return coder.decode(encoded);
	}

}

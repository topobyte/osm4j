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

import java.util.HashMap;
import java.util.Map;

import de.topobyte.osm4j.extra.nodearray.util.Coder;
import de.topobyte.osm4j.extra.nodearray.util.Coders;
import de.topobyte.osm4j.extra.nodearray.util.Interval;
import de.topobyte.osm4j.extra.nodearray.util.Intervals;

public class FindErrorDelta
{

	public static void main(String[] args)
	{
		int nTests = 100000;

		Map<String, Coder<?>> nameToCoder = new HashMap<>();
		nameToCoder.put("int lon", Coders.CODER_INT_LON);
		nameToCoder.put("int lat", Coders.CODER_INT_LAT);
		nameToCoder.put("short lon", Coders.CODER_SHORT_LON);
		nameToCoder.put("short lat", Coders.CODER_SHORT_LAT);

		for (String name : nameToCoder.keySet()) {
			Coder<?> coder = nameToCoder.get(name);
			double maxError = findError(nTests, coder);
			System.out.println(name + ": " + maxError);
		}
	}

	private static <T extends Number> double findError(int nTests,
			Coder<T> coder)
	{
		Interval interval = coder.interval();
		double maxError = 0;
		for (int i = 0; i < nTests; i++) {
			double value = Intervals.random(interval);
			T encoded = coder.encode(value);
			double decoded = coder.decode(encoded);
			double error = Math.abs(value - decoded);
			maxError = Math.max(maxError, error);
		}
		return maxError;
	}

}

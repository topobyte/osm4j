// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.osc.test.minutes;

import java.io.IOException;
import java.net.MalformedURLException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.osc.ReplicationInfo;
import de.topobyte.osm4j.osc.ReplicationUtil;

public class TestMinuteFind
{

	@Test
	public void test() throws IOException, OsmInputException
	{

		test(new DateTime(2015, 9, 17, 12, 36, 13, DateTimeZone.UTC), 1575907);
		test(new DateTime(2015, 9, 17, 18, 2, 1, DateTimeZone.UTC), 1576233);
		test(new DateTime(2017, 8, 13, 15, 44, 02, DateTimeZone.UTC), 2576233);
	}

	private void test(DateTime needle, long expectedSequenceNumber)
			throws MalformedURLException, IOException
	{
		ReplicationInfo last = ReplicationUtil.getMinuteInfo();
		ReplicationInfo first = ReplicationUtil.getMinuteInfo(1);
		ReplicationInfo found = find(last, first, needle);
		Assert.assertEquals(expectedSequenceNumber, found.getSequenceNumber());
	}

	private ReplicationInfo find(ReplicationInfo high, ReplicationInfo low,
			DateTime needle) throws MalformedURLException, IOException
	{
		System.out.println("Searching: " + needle);
		System.out.println("Searching between:");
		print(high);
		print(low);

		Minutes minutes = Minutes.minutesBetween(low.getTime(), high.getTime());
		System.out.println(minutes.getMinutes());

		while (true) {
			long midNum = (high.getSequenceNumber() + low.getSequenceNumber())
					/ 2;
			ReplicationInfo mid = ReplicationUtil.getMinuteInfo(midNum);

			if (needle.isBefore(mid.getTime())) {
				high = mid;
			} else {
				low = mid;
			}
			print(high);
			print(low);
			if (high.getSequenceNumber() - low.getSequenceNumber() < 2) {
				break;
			}
		}

		return low;
	}

	private void print(ReplicationInfo info)
	{
		System.out.println(String.format("%s: %d", info.getTime(),
				info.getSequenceNumber()));
	}

}

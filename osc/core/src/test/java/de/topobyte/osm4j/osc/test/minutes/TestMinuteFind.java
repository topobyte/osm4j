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

import org.joda.time.LocalDateTime;
import org.junit.Test;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.osc.ReplicationInfo;
import de.topobyte.osm4j.osc.ReplicationUtil;

public class TestMinuteFind
{

	@Test
	public void test() throws IOException, OsmInputException
	{
		ReplicationInfo last = ReplicationUtil.getMinuteInfo();
		ReplicationInfo first = ReplicationUtil.getMinuteInfo(1);

		LocalDateTime needle = new LocalDateTime(2015, 9, 17, 14, 36, 13);
		find(last, first, needle);

	}

	private void find(ReplicationInfo high, ReplicationInfo low,
			LocalDateTime needle)
	{
		System.out.println("Searching: " + needle);
		System.out.println("Searching between:");
		print(high);
		print(low);
		// TODO: implement binary search
	}

	private void print(ReplicationInfo info)
	{
		System.out.println(String.format("%s: %d", info.getTime(),
				info.getSequenceNumber()));
	}

}

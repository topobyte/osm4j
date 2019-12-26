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
		ReplicationUtil util = new ReplicationUtil();
		ReplicationInfo found = util.findMinute(needle);
		util.closeHttpClient();
		Assert.assertEquals(expectedSequenceNumber, found.getSequenceNumber());
	}

}

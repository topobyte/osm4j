// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.replication.test.changesets;

import java.io.IOException;
import java.net.MalformedURLException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.replication.ReplicationInfo;
import de.topobyte.osm4j.replication.ReplicationUtil;

public class TestChangesetFind
{

	@Test
	public void test() throws IOException
	{
		// attention, we're finding the sequence number here, the file name is
		// one higher
		test(new DateTime(2018, 11, 13, 11, 39, 40, DateTimeZone.UTC), 3150861);
		test(new DateTime(2021, 01, 14, 13, 34, 20, DateTimeZone.UTC), 4280122);
		test(new DateTime(2016, 11, 26, 22, 22, 20, DateTimeZone.UTC), 2123456);
	}

	private void test(DateTime needle, long expectedSequenceNumber)
			throws MalformedURLException, IOException
	{
		ReplicationUtil util = new ReplicationUtil();
		ReplicationInfo found = util.findChangeset(needle);
		util.closeHttpClient();
		Assert.assertEquals(expectedSequenceNumber, found.getSequenceNumber());
	}

}

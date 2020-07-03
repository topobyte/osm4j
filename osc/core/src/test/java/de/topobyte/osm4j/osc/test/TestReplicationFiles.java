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

package de.topobyte.osm4j.osc.test;

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.osc.ReplicationFiles;

public class TestReplicationFiles
{

	@Test
	public void testMinute()
	{
		String expected = "https://planet.openstreetmap.org/replication/minute/003/392/499.osc.gz";
		String file = ReplicationFiles.minute(3392499);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testHour()
	{
		String expected = "https://planet.openstreetmap.org/replication/hour/000/056/745.osc.gz";
		String file = ReplicationFiles.hour(56745);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testDay()
	{
		String expected = "https://planet.openstreetmap.org/replication/day/000/002/364.osc.gz";
		String file = ReplicationFiles.day(2364);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testChangesets()
	{
		String expected = "https://planet.openstreetmap.org/replication/changesets/003/309/504.osm.gz";
		String file = ReplicationFiles.changesets(3309504);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testMinuteState()
	{
		String expected = "https://planet.openstreetmap.org/replication/minute/003/392/499.state.txt";
		String file = ReplicationFiles.minuteState(3392499);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testHourState()
	{
		String expected = "https://planet.openstreetmap.org/replication/hour/000/056/745.state.txt";
		String file = ReplicationFiles.hourState(56745);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testDayState()
	{
		String expected = "https://planet.openstreetmap.org/replication/day/000/002/364.state.txt";
		String file = ReplicationFiles.dayState(2364);
		Assert.assertEquals(expected, file);
	}

	@Test
	public void testChangesetsState()
	{
		String expected = "https://planet.openstreetmap.org/replication/changesets/003/309/504.state.txt";
		String file = ReplicationFiles.changesetsState(3309504);
		Assert.assertEquals(expected, file);
	}

}

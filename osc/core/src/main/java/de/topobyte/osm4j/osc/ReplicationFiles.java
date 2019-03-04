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

package de.topobyte.osm4j.osc;

public class ReplicationFiles
{

	public static String minute(long num)
	{
		return file("minute", num, "osc.gz");
	}

	public static String minuteState(long num)
	{
		return file("minute", num, "state.txt");
	}

	public static String hour(long num)
	{
		return file("hour", num, "osc.gz");
	}

	public static String hourState(long num)
	{
		return file("hour", num, "state.txt");
	}

	public static String day(long num)
	{
		return file("day", num, "osc.gz");
	}

	public static String dayState(long num)
	{
		return file("day", num, "state.txt");
	}

	public static String changesets(long num)
	{
		return file("changesets", num, "osm.gz");
	}

	public static String changesetsState(int num)
	{
		return file("changesets", num, "state.txt");
	}

	public static String file(String type, long num, String extension)
	{
		long p3 = num % 1000;
		long p2 = (num / 1000) % 1000;
		long p1 = num / 1000000;
		return String.format(
				"https://planet.openstreetmap.org/replication/%s/%03d/%03d/%03d.%s",
				type, p1, p2, p3, extension);
	}

	public static String minuteState()
	{
		return "https://planet.openstreetmap.org/replication/minute/state.txt";
	}

	public static String hourState()
	{
		return "https://planet.openstreetmap.org/replication/hour/state.txt";
	}

	public static String dayState()
	{
		return "https://planet.openstreetmap.org/replication/day/state.txt";
	}

	public static String changesetsState()
	{
		return "https://planet.openstreetmap.org/replication/minute/state.yaml";
	}

}

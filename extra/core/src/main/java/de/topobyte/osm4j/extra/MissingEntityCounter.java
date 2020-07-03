// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra;

import java.util.ArrayList;
import java.util.List;

import com.slimjars.dist.gnu.trove.list.TIntList;
import com.slimjars.dist.gnu.trove.list.array.TIntArrayList;

public class MissingEntityCounter
{

	private int numNodes = 0;
	private int numWays = 0;
	private int numWayNodes = 0;

	public void addNodes(int n)
	{
		numNodes += n;
	}

	public void addWays(int n)
	{
		numWays += n;
	}

	public void addWayNodes(int n)
	{
		numWayNodes += n;
	}

	public int getNumNodes()
	{
		return numNodes;
	}

	public int getNumWays()
	{
		return numWays;
	}

	public int getNumWayNodes()
	{
		return numWayNodes;
	}

	public boolean nonZero()
	{
		return numNodes != 0 || numWays != 0 || numWayNodes != 0;
	}

	public String toMessage()
	{
		TIntList nums = new TIntArrayList();
		List<String> names = new ArrayList<>();
		if (numNodes != 0) {
			names.add("nodes");
			nums.add(numNodes);
		}
		if (numWays != 0) {
			names.add("ways");
			nums.add(numWays);
		}
		if (numWayNodes != 0) {
			names.add("waynodes");
			nums.add(numWayNodes);
		}

		int vals = nums.size();
		if (vals == 0) {
			return null;
		}

		StringBuilder buffer = new StringBuilder();

		buffer.append(nums.get(0));
		buffer.append(" ");
		buffer.append(names.get(0));

		for (int i = 1; i < vals - 1; i++) {
			buffer.append(", ");
			buffer.append(nums.get(i));
			buffer.append(" ");
			buffer.append(names.get(i));
		}

		if (vals > 1) {
			buffer.append(" and ");
			buffer.append(nums.get(vals - 1));
			buffer.append(" ");
			buffer.append(names.get(vals - 1));
		}

		return buffer.toString();
	}
}

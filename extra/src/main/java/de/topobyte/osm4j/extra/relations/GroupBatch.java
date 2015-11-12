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

package de.topobyte.osm4j.extra.relations;

import java.util.ArrayList;
import java.util.List;

class GroupBatch
{

	private int maxMembers;

	private List<Group> groups = new ArrayList<>();
	private int members = 0;

	GroupBatch(int maxMembers)
	{
		this.maxMembers = maxMembers;
	}

	void clear()
	{
		groups.clear();
		members = 0;
	}

	boolean fits(Group group)
	{
		if (groups.isEmpty()) {
			return true;
		}
		if (members + group.getNumMembers() <= maxMembers) {
			return true;
		}
		return false;
	}

	void add(Group group)
	{
		groups.add(group);
		members += group.getNumMembers();
	}

	List<Group> getGroups()
	{
		return groups;
	}

	int getNumMembers()
	{
		return members;
	}

	boolean isFull()
	{
		return members == maxMembers;
	}

}

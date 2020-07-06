// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.waydb;

import java.util.Map;

import com.slimjars.dist.gnu.trove.list.TLongList;

import de.topobyte.osm4j.diskstorage.vardb.Record;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public abstract class AbstractWayRecord extends Record
{

	/**
	 * @return the way's id
	 */
	@Override
	public abstract long getId();

	/**
	 * @return the list of references node ids
	 */
	public abstract TLongList getNodeIds();

	/**
	 * @return the map of tags.
	 */
	public abstract Map<String, String> getTags();

}

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

package de.topobyte.osm4j.core.model.iface;

/**
 * Common interface for the three basic OpenStreetMap types. It defines all
 * traits common to the basic types, i.e. an integer identifier, a list of tags
 * and optional meta data.
 */
public interface OsmEntity
{

	public long getId();

	public int getNumberOfTags();

	public OsmTag getTag(int n);

	public OsmMetadata getMetadata();

}

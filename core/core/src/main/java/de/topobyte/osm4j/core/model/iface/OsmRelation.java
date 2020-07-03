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
 * Interface for OpenStreetMap relations. A relation is a list of references to
 * other objects, also called the members of the relation. Each reference points
 * to some object (defined by its type and its identifier) and has an additional
 * role to describe the type of membership.
 */
public interface OsmRelation extends OsmEntity
{

	public int getNumberOfMembers();

	public OsmRelationMember getMember(int n);

}

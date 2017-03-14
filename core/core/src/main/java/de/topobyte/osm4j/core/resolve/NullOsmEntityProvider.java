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

package de.topobyte.osm4j.core.resolve;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class NullOsmEntityProvider implements OsmEntityProvider
{

	private static String MESSAGE = "The null provider does not contain any data";

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException(MESSAGE);
	}

	@Override
	public OsmWay getWay(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException(MESSAGE);
	}

	@Override
	public OsmRelation getRelation(long id) throws EntityNotFoundException
	{
		throw new EntityNotFoundException(MESSAGE);
	}

}

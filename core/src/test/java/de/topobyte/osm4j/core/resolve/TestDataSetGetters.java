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

package de.topobyte.osm4j.core.resolve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.test.Loader;

public class TestDataSetGetters extends BaseTestDataSets
{

	public TestDataSetGetters(Loader loader)
	{
		super(loader);
	}

	@Test
	public void getNodeSuccessful() throws EntityNotFoundException
	{
		OsmNode node = data.getNode(1);
		assertEquals(node.getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getNodeException() throws EntityNotFoundException
	{
		data.getNode(4);
	}

	@Test
	public void getWaySuccessful() throws EntityNotFoundException
	{
		OsmWay way = data.getWay(1);
		assertEquals(way.getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getWayException() throws EntityNotFoundException
	{
		data.getWay(5);
	}

	@Test
	public void getRelationSuccessful() throws EntityNotFoundException
	{
		OsmRelation relation = data.getRelation(1);
		assertEquals(relation.getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getRelationException() throws EntityNotFoundException
	{
		data.getRelation(3);
	}

}

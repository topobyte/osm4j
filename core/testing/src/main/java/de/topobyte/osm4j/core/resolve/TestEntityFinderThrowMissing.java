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

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.test.Loader;
import de.topobyte.osm4j.core.test.TroveUtil;

public class TestEntityFinderThrowMissing extends BaseTestDataSets
{

	private EntityFinder finder;

	public TestEntityFinderThrowMissing(Loader loader)
	{
		super(loader);
	}

	@Override
	@Before
	public void prepare() throws IOException
	{
		super.prepare();
		finder = EntityFinders.create(data, EntityNotFoundStrategy.THROW);
	}

	@Test
	public void getNodeSuccessful() throws EntityNotFoundException
	{
		List<OsmNode> nodes = finder.findNodes(TroveUtil.collection(1));
		assertEquals(nodes.get(0).getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getNodeException() throws EntityNotFoundException
	{
		finder.findNodes(TroveUtil.collection(4));
	}

	@Test
	public void getWaySuccessful() throws EntityNotFoundException
	{
		List<OsmWay> ways = finder.findWays(TroveUtil.collection(1));
		assertEquals(ways.get(0).getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getWayException() throws EntityNotFoundException
	{
		finder.findWays(TroveUtil.collection(5));
	}

	@Test
	public void getRelationSuccessful() throws EntityNotFoundException
	{
		List<OsmRelation> relations = finder.findRelations(TroveUtil
				.collection(1));
		assertEquals(relations.get(0).getId(), 1);
	}

	@Test(expected = EntityNotFoundException.class)
	public void getRelationException() throws EntityNotFoundException
	{
		finder.findRelations(TroveUtil.collection(3));
	}

}

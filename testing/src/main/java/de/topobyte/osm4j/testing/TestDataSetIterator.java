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

package de.topobyte.osm4j.testing;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestWay;

public class TestDataSetIterator implements OsmIterator
{

	private TestDataSet data;

	private int index = 0;

	public TestDataSetIterator(TestDataSet data)
	{
		this.data = data;
	}

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasBounds()
	{
		return data.hasBounds();
	}

	@Override
	public OsmBounds getBounds()
	{
		return data.getBounds();
	}

	@Override
	public boolean hasNext()
	{
		List<TestNode> nodes = data.getNodes();
		List<TestWay> ways = data.getWays();
		List<TestRelation> relations = data.getRelations();
		return index < nodes.size() + ways.size() + relations.size();
	}

	@Override
	public EntityContainer next()
	{
		List<TestNode> nodes = data.getNodes();
		List<TestWay> ways = data.getWays();
		List<TestRelation> relations = data.getRelations();
		if (index < nodes.size()) {
			return new EntityContainer(EntityType.Node, nodes.get(index++));
		}
		int wayIndex = index - nodes.size();
		if (wayIndex < ways.size()) {
			index++;
			return new EntityContainer(EntityType.Way, ways.get(wayIndex));
		}
		int relationIndex = wayIndex - ways.size();
		if (relationIndex < relations.size()) {
			index++;
			return new EntityContainer(EntityType.Relation,
					relations.get(relationIndex));
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}

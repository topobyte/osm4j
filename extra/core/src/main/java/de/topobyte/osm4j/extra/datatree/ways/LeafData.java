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

package de.topobyte.osm4j.extra.datatree.ways;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.core.resolve.UnionOsmEntityProvider;
import de.topobyte.osm4j.extra.datatree.Node;

public class LeafData
{

	private Node leaf;
	private InMemoryListDataSet dataWays;
	private InMemoryListDataSet dataNodes1;
	private InMemoryListDataSet dataNodes2;

	public LeafData(Node leaf, InMemoryListDataSet dataWays,
			InMemoryListDataSet dataNodes1, InMemoryListDataSet dataNodes2)
	{
		this.leaf = leaf;
		this.dataWays = dataWays;
		this.dataNodes1 = dataNodes1;
		this.dataNodes2 = dataNodes2;
	}

	public Node getLeaf()
	{
		return leaf;
	}

	public InMemoryListDataSet getDataWays()
	{
		return dataWays;
	}

	public InMemoryListDataSet getDataNodes1()
	{
		return dataNodes1;
	}

	public InMemoryListDataSet getDataNodes2()
	{
		return dataNodes2;
	}

	public OsmEntityProvider getNodeProvider()
	{
		List<OsmEntityProvider> providers = new ArrayList<>();
		providers.add(dataNodes1);
		providers.add(dataNodes2);
		return new UnionOsmEntityProvider(providers);
	}

}

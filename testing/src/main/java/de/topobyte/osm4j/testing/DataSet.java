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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.ImplUtil;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;

public class DataSet
{

	private List<OsmNode> nodes = new ArrayList<>();
	private List<OsmWay> ways = new ArrayList<>();
	private List<OsmRelation> relations = new ArrayList<>();

	public DataSet()
	{
	}

	public DataSet(DataSet data)
	{
		for (OsmNode node : data.getNodes()) {
			nodes.add(ImplUtil.clone(node));
		}
		for (OsmWay way : data.getWays()) {
			ways.add(ImplUtil.clone(way));
		}
		for (OsmRelation relation : data.getRelations()) {
			relations.add(ImplUtil.clone(relation));
		}
	}

	public DataSet(InMemoryDataSet data)
	{
		long[] nodeIds = data.getNodes().keys();
		Arrays.sort(nodeIds);
		for (int i = 0; i < nodeIds.length; i++) {
			nodes.add(data.getNodes().get(nodeIds[i]));
		}

		long[] wayIds = data.getWays().keys();
		Arrays.sort(wayIds);
		for (int i = 0; i < wayIds.length; i++) {
			ways.add(data.getWays().get(wayIds[i]));
		}

		long[] relationIds = data.getRelations().keys();
		Arrays.sort(relationIds);
		for (int i = 0; i < relationIds.length; i++) {
			relations.add(data.getRelations().get(relationIds[i]));
		}
	}

	public List<OsmNode> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<OsmNode> nodes)
	{
		this.nodes = nodes;
	}

	public List<OsmWay> getWays()
	{
		return ways;
	}

	public void setWays(List<OsmWay> ways)
	{
		this.ways = ways;
	}

	public List<OsmRelation> getRelations()
	{
		return relations;
	}

	public void setRelations(List<OsmRelation> relations)
	{
		this.relations = relations;
	}

}

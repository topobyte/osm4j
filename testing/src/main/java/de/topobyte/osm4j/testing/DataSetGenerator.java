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

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class DataSetGenerator
{

	private EntityGenerator entityGenerator;

	public DataSetGenerator(EntityGenerator entityGenerator)
	{
		this.entityGenerator = entityGenerator;
	}

	public DataSet generate(int numNodes, int numWays, int numRelations)
	{
		DataSet dataSet = new DataSet();
		for (int i = 0; i < numNodes; i++) {
			OsmNode node = entityGenerator.generateNode();
			dataSet.getNodes().add(node);
		}
		for (int i = 0; i < numWays; i++) {
			OsmWay way = entityGenerator.generateWay();
			dataSet.getWays().add(way);
		}

		for (int i = 0; i < numRelations; i++) {
			OsmRelation relation = entityGenerator.generateRelation();
			dataSet.getRelations().add(relation);
		}
		return dataSet;
	}

}

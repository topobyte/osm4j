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

package de.topobyte.osm4j.extra.extracts.query;

import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.resolve.CompositeOsmEntityProvider;
import de.topobyte.osm4j.geometry.LineworkBuilder;
import de.topobyte.osm4j.geometry.RegionBuilder;

public abstract class AbstractRelationsQuery
{

	protected InMemoryListDataSet dataNodes;
	protected InMemoryListDataSet dataWays;
	protected InMemoryListDataSet dataRelations;

	protected PredicateEvaluator test;

	protected boolean fastRelationTests;

	protected CompositeOsmEntityProvider provider;

	protected GeometryFactory factory = new GeometryFactory();
	protected LineworkBuilder lineworkBuilder = new LineworkBuilder(factory);
	protected RegionBuilder regionBuilder = new RegionBuilder(factory);

	public AbstractRelationsQuery(InMemoryListDataSet dataNodes,
			InMemoryListDataSet dataWays, InMemoryListDataSet dataRelations,
			PredicateEvaluator test, boolean fastRelationTests)
	{
		this.dataNodes = dataNodes;
		this.dataWays = dataWays;
		this.dataRelations = dataRelations;
		this.test = test;
		this.fastRelationTests = fastRelationTests;

		provider = new CompositeOsmEntityProvider(dataNodes, dataWays,
				dataRelations);
	}

}

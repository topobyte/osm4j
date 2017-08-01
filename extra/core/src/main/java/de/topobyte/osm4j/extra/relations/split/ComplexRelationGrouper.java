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

package de.topobyte.osm4j.extra.relations.split;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;
import com.slimjars.dist.gnu.trove.set.TLongSet;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.util.RelationIterator;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;

public class ComplexRelationGrouper
{

	private OsmIteratorInputFactory iteratorFactory;

	private RelationGraph relationGraph;
	private List<Group> groups;
	private TLongObjectMap<OsmRelation> groupRelations;

	public ComplexRelationGrouper(OsmIteratorInputFactory iteratorFactory,
			boolean storeSimpleRelations, boolean undirected)
	{
		this.iteratorFactory = iteratorFactory;
		relationGraph = new RelationGraph(storeSimpleRelations, undirected);
	}

	public List<Group> getGroups()
	{
		return groups;
	}

	public TLongObjectMap<OsmRelation> getGroupRelations()
	{
		return groupRelations;
	}

	public void buildGroups() throws IOException
	{
		OsmIteratorInput iteratorInput = iteratorFactory.createIterator(false,
				false);
		relationGraph.build(iteratorInput.getIterator());
		iteratorInput.close();

		System.out.println("Number of relations without relation members: "
				+ relationGraph.getNumNoChildren());
		System.out.println("Number of relations with relation members: "
				+ relationGraph.getIdsHasChildRelations().size());
		System.out.println("Number of child relations: "
				+ relationGraph.getIdsIsChildRelation().size());

		groups = relationGraph.buildGroups();
	}

	public void readGroupRelations(boolean readMetadata)
			throws FileNotFoundException, IOException
	{
		TLongSet idsHasChildRelations = relationGraph.getIdsHasChildRelations();
		TLongSet idsIsChildRelation = relationGraph.getIdsIsChildRelation();

		OsmIteratorInput iteratorInput = iteratorFactory.createIterator(true,
				readMetadata);
		RelationIterator relations = new RelationIterator(
				iteratorInput.getIterator());

		groupRelations = new TLongObjectHashMap<>();
		for (OsmRelation relation : relations) {
			if (idsHasChildRelations.contains(relation.getId())
					|| idsIsChildRelation.contains(relation.getId())) {
				groupRelations.put(relation.getId(), relation);
			}
		}

		iteratorInput.close();
	}

}

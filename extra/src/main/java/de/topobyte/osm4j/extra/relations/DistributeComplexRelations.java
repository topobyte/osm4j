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

package de.topobyte.osm4j.extra.relations;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.CompositeOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.InMemoryDataSet;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.OsmIoUtils;

public class DistributeComplexRelations extends DistributeSimpleBase
{

	@Override
	protected String getHelpMessage()
	{
		return DistributeComplexRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		DistributeComplexRelations task = new DistributeComplexRelations();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	@Override
	protected void build(Path path) throws IOException
	{
		Path pathRelations = path.resolve(fileNamesRelations);
		Path pathWays = path.resolve(fileNamesWays);
		Path pathNodes = path.resolve(fileNamesNodes);

		RelationGraph relationGraph = new RelationGraph(false, true);

		InputStream input = StreamUtil.bufferedInputStream(pathRelations
				.toFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				false);
		relationGraph.build(iterator);
		input.close();

		InMemoryDataSet dataRelations = read(pathRelations, true, true);

		System.out.println("Number of relations without relation members: "
				+ relationGraph.getNumNoChildren());
		System.out.println("Number of relations with relation members: "
				+ relationGraph.getIdsHasChildRelations().size());
		System.out.println("Number of child relations: "
				+ relationGraph.getIdsIsChildRelation().size());

		List<Group> groups = relationGraph.buildGroups();
		System.out.println("number of groups: " + groups.size());

		List<RelationGroup> relationGroups = new ArrayList<>();

		for (Group group : groups) {
			try {
				List<OsmRelation> groupRelations = findRelations(
						group.getRelationIds(), dataRelations);
				relationGroups.add(new RelationGroupMultiple(groupRelations));
			} catch (EntityNotFoundException e) {
				System.out.println("unable to build relation group");
			}
		}

		if (relationGroups.size() == 1) {
			InMemoryDataSet dataNodes = read(pathNodes, false, false);
			Geometry box = box(dataNodes.getNodes().valueCollection());

			List<Node> leafs = tree.query(box);

			RelationGroup relation = relationGroups.get(0);

			if (leafs.size() == 1) {
				nWrittenToTree++;
				write(relation, outputs.get(leafs.get(0)));
			} else {
				nRemaining++;
				write(relation, outputNonTree);
			}
		} else {
			InMemoryDataSet dataNodes = read(pathNodes, false, false);
			InMemoryDataSet dataWays = read(pathWays, false, false);

			OsmEntityProvider entityProvider = new CompositeOsmEntityProvider(
					dataNodes, dataWays, dataRelations);

			for (RelationGroup relation : relationGroups) {
				Set<OsmNode> nodes;
				try {
					nodes = relation.findNodes(entityProvider);
					Geometry box = box(nodes);
					List<Node> leafs = tree.query(box);

					if (leafs.size() == 1) {
						nWrittenToTree++;
						write(relation, outputs.get(leafs.get(0)));
					} else {
						nRemaining++;
						write(relation, outputNonTree);
					}
				} catch (EntityNotFoundException e) {
					//
				}
			}

		}
	}

	private void write(RelationGroup group, Output output) throws IOException
	{
		Collection<OsmRelation> relations = group.getRelations();
		for (OsmRelation relation : relations) {
			output.getOsmOutput().write(relation);
		}
	}

	private List<OsmRelation> findRelations(TLongSet ids,
			OsmEntityProvider entityProvider) throws EntityNotFoundException
	{
		List<OsmRelation> relations = new ArrayList<>();
		TLongIterator idIterator = ids.iterator();
		while (idIterator.hasNext()) {
			relations.add(entityProvider.getRelation(idIterator.next()));
		}
		return relations;
	}

}

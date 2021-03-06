// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.utils.areafilter;

import java.io.IOException;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;

import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.jts.utils.predicate.PredicateEvaluator;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.ProgressMonitor;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public abstract class AbstractAreaFilter
{

	private OsmOutputStream output;
	private OsmIterator input;

	private boolean onlyNodes;

	private ProgressMonitor monitor = new ProgressMonitor("area filter");

	protected PredicateEvaluator test;

	private TLongHashSet nodeIds = new TLongHashSet();
	private TLongHashSet wayIds = new TLongHashSet();

	public AbstractAreaFilter(OsmOutputStream output, OsmIterator input,
			boolean onlyNodes)
	{
		this.output = output;
		this.input = input;
		this.onlyNodes = onlyNodes;
	}

	public void run() throws IOException
	{
		Iterator<EntityContainer> iterator = input.iterator();
		while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				handle((OsmNode) entityContainer.getEntity());
				break;
			case Way:
				handle((OsmWay) entityContainer.getEntity());
				break;
			case Relation:
				handle((OsmRelation) entityContainer.getEntity());
				break;
			}
		}
		output.complete();
	}

	private void handle(OsmNode node) throws IOException
	{
		monitor.nodeProcessed();
		Coordinate coordinate = new Coordinate(node.getLongitude(),
				node.getLatitude());
		if (test.covers(coordinate)) {
			if (!onlyNodes) {
				nodeIds.add(node.getId());
			}
			output.write(node);
		}
	}

	private void handle(OsmWay way) throws IOException
	{
		monitor.wayProcessed();
		if (onlyNodes) {
			return;
		}
		boolean take = false;
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			if (nodeIds.contains(way.getNodeId(i))) {
				take = true;
				break;
			}
		}
		if (take) {
			wayIds.add(way.getId());
			output.write(way);
		}
	}

	private void handle(OsmRelation relation) throws IOException
	{
		monitor.relationProcessed();
		if (onlyNodes) {
			return;
		}
		boolean take = false;
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (member.getType() == EntityType.Node) {
				if (nodeIds.contains(member.getId())) {
					take = true;
					break;
				}
			} else if (member.getType() == EntityType.Way) {
				if (wayIds.contains(member.getId())) {
					take = true;
					break;
				}
			}
		}
		if (take) {
			output.write(relation);
		}
	}

}

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

package de.topobyte.osm4j.utils;

import gnu.trove.set.hash.TLongHashSet;

import java.io.IOException;

import com.vividsolutions.jts.geom.Coordinate;

import de.topobyte.jts.utils.predicate.ContainmentTest;
import de.topobyte.osm4j.core.access.ProgressMonitor;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractAreaFilter extends
		AbstractTaskSingleInputIteratorSingleOutput
{

	private static final String OPTION_ONLY_NODES = "nodes_only";

	private boolean onlyNodes;

	private ProgressMonitor monitor = new ProgressMonitor("bboxfilter");

	protected ContainmentTest test;
	private TLongHashSet nodeIds = new TLongHashSet();
	private TLongHashSet wayIds = new TLongHashSet();

	public AbstractAreaFilter()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_ONLY_NODES, false, false, "extract only nodes");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		onlyNodes = false;
		onlyNodes = line.hasOption(OPTION_ONLY_NODES);
	}

	protected void run() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer entityContainer = inputIterator.next();
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
		osmOutputStream.complete();
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
			osmOutputStream.write(node);
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
			osmOutputStream.write(way);
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
			osmOutputStream.write(relation);
		}
	}

}

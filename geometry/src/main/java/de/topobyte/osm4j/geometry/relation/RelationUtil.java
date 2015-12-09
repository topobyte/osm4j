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

package de.topobyte.osm4j.geometry.relation;

import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.multicollections.CountingMultiValMap;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;

public class RelationUtil
{

	final static Logger logger = LoggerFactory.getLogger(RelationUtil.class);

	/**
	 * Given a set of ways and their head's and tail's in the wayTailMap,
	 * construct valid rings by combining ways with same tail-ids.
	 */
	public static List<ChainOfWays> buildRings(MultiSet<OsmWay> ways,
			CountingMultiValMap<Long, OsmWay> wayTailMap)
	{
		List<ChainOfWays> rings = new ArrayList<>();

		while (ways.keySet().size() > 0) {

			// Pick a way as part of a new ring
			OsmWay way = ways.keySet().iterator().next();
			ChainOfWays ring = new ChainOfWays(way);
			rings.add(ring);

			// Remove it from the set of ways...
			ways.remove(way);
			// ...and from the tail map.
			long id0 = way.getNodeId(0);
			long idN = way.getNodeId(way.getNumberOfNodes() - 1);
			wayTailMap.remove(id0, way);
			wayTailMap.remove(idN, way);

			// Try to assemble a ring for the way
			while (!ring.isClosed()) {
				long last = ring.getLast();
				Set<OsmWay> waySet = wayTailMap.getForKey(last);
				boolean accept = false;
				Iterator<OsmWay> iter = waySet.iterator();
				OsmWay take = null;
				while (iter.hasNext()) {
					take = iter.next();
					if (ring.getWaySet().contains(take)) {
						// do not allow the same way to be included twice.
						continue;
					}
					accept = true;
					break;
				}
				if (!accept) {
					// Unable to find a way to extend the chain of ways
					// System.out.println("none found for: " + last + "," +
					// ring.getFirst() +
					// " size: " + ring.segments.size());
					// System.out.println(ring.segments.get(ring.segments.size()-1).getWay().getId());
					break;
				}
				if (take == null) {
					// can't be null, but avoid warning.
					break;
				}

				// Extend the current ring with the found way..
				ring.addWay(take);

				// And remove it from the set of ways...
				ways.remove(take);
				// ...and from the tail map.
				long tid0 = take.getNodeId(0);
				long tidN = take.getNodeId(take.getNumberOfNodes() - 1);
				wayTailMap.remove(tid0, take);
				wayTailMap.remove(tidN, take);
			}
		}

		return rings;
	}

	/**
	 * For all ways given, build a multivalmap from node-ids to ways. Head and
	 * tail of each way are put as key and the way as value.
	 */
	public static CountingMultiValMap<Long, OsmWay> buildWayTailMap(
			MultiSet<OsmWay> ways)
	{
		CountingMultiValMap<Long, OsmWay> waysNodes = new CountingMultiValMap<>();
		for (OsmWay way : ways) {
			// add all of them to waysNodes. implement iterator correctly...
			int nnodes = way.getNumberOfNodes();
			if (nnodes < 2) {
				throw new IllegalArgumentException(
						"Only ways with 2 or more nodes are allowed");
			}
			long node1 = way.getNodeId(0);
			long nodeN = way.getNodeId(nnodes - 1);
			waysNodes.add(node1, way);
			waysNodes.add(nodeN, way);
		}
		return waysNodes;
	}

	/**
	 * For each ring in this collection of rings, check whether it is closed. If
	 * not, print some status information.
	 * 
	 * @param missingEntitiesStrategy
	 * 
	 * @return whether all rings are closed.
	 */
	public static void checkRings(Collection<ChainOfWays> rings,
			OsmEntityProvider resolver,
			MissingEntitiesStrategy missingEntitiesStrategy)
			throws EntityNotFoundException
	{
		for (ChainOfWays ring : rings) {
			if (ring.isClosed()) {
				continue;
			}
			List<WaySegment> segments = ring.getSegments();
			int len = 0;
			for (WaySegment rs : segments) {
				len += rs.getWay().getNumberOfNodes();
			}
			WaySegment seg0 = segments.get(0);
			WaySegment segN = segments.get(segments.size() - 1);
			long nodeId1 = seg0.getNodeId(0);
			long nodeIdN = segN.getNodeId(segN.getNumberOfNodes() - 1);

			try {
				OsmNode node1 = resolver.getNode(nodeId1);
				OsmNode nodeN = resolver.getNode(nodeIdN);

				logger.debug("we have an unclosed ring of size " + len);
				logger.debug(String.format("start/end: %f,%f %f,%f",
						node1.getLongitude(), node1.getLatitude(),
						nodeN.getLongitude(), nodeN.getLatitude()));
			} catch (EntityNotFoundException e) {
				switch (missingEntitiesStrategy) {
				case BUILD_PARTIAL:
					continue;
				default:
				case BUILD_EMPTY:
				case THROW_EXCEPTION:
					throw (e);
				}
			}
		}
	}

	public static List<ChainOfNodes> convertToSegmentRings(
			List<ChainOfWays> rings)
	{
		List<ChainOfNodes> segmentRings = new ArrayList<>();
		for (ChainOfWays ring : rings) {
			segmentRings.add(ring.toSegmentRing());
		}
		return segmentRings;
	}

	public static void closeUnclosedRingWithStraightLine(
			Collection<ChainOfWays> rings)
	{
		for (ChainOfWays ring : rings) {
			if (!ring.isClosed()) {
				logger.debug("unclosed ring with " + ring.getSegments().size()
						+ " segments");
				List<WaySegment> segments = ring.getSegments();
				WaySegment rs1 = segments.get(0);
				WaySegment rs2 = segments.get(segments.size() - 1);

				long n1 = rs1.getNodeId(0);
				long n2 = rs2.getNodeId(rs2.getNumberOfNodes() - 1);

				TLongArrayList ids = new TLongArrayList();
				ids.add(n1);
				ids.add(n2);
				OsmWay filler = new Way(0L, ids);
				ring.addWay(filler);
			}
		}
	}

	public static void convertToSegmentChainsAndResolveNodeIntersections(
			List<ChainOfWays> chains, List<ChainOfNodes> outNoIntersections,
			List<ChainOfNodes> outRings, List<ChainOfNodes> outNonRings)
	{
		for (ChainOfNodes s : RelationUtil.convertToSegmentRings(chains)) {
			if (!s.hasNodeIntersections()) {
				outNoIntersections.add(s);
			} else {
				for (ChainOfNodes t : s.resolveNodeIntersections()) {
					if (t.isValidRing()) {
						outRings.add(t);
					} else if (t.getLength() > 1) {
						outNonRings.add(t);
					}
				}
			}
		}
	}

}

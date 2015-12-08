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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.LinearRing;

import de.topobyte.adt.multicollections.CountingMultiValMap;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class RelationUtil
{

	final static Logger logger = LoggerFactory.getLogger(RelationUtil.class);

	/**
	 * Given a set of ways and their head's and tail's in the wayTailMap,
	 * construct valid rings by combining ways with same tail-ids.
	 */
	public static List<WayRing> buildRings(MultiSet<OsmWay> ways,
			CountingMultiValMap<Long, OsmWay> wayTailMap)
	{
		List<WayRing> rings = new ArrayList<WayRing>();

		while (ways.keySet().size() > 0) {

			// Pick a way as part of a new ring
			OsmWay way = ways.keySet().iterator().next();
			WayRing ring = new WayRing(way);
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
		CountingMultiValMap<Long, OsmWay> waysNodes = new CountingMultiValMap<Long, OsmWay>();
		for (OsmWay way : ways) {
			// add all of them to waysNodes. implement iterator correctly...
			int nnodes = way.getNumberOfNodes();
			if (nnodes == 0) {
				logger.debug("zero size list....");
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
	 * @return whether all rings are closed.
	 */
	public static boolean checkRings(Collection<WayRing> rings,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		boolean allClosed = true;
		for (WayRing ring : rings) {
			if (ring.isClosed()) {
				continue;
			}
			allClosed = false;
			List<WayRingSegment> segments = ring.getSegments();
			int len = 0;
			for (WayRingSegment rs : segments) {
				len += rs.getWay().getNumberOfNodes();
			}
			WayRingSegment seg0 = segments.get(0);
			WayRingSegment segN = segments.get(segments.size() - 1);
			long nodeId1 = seg0
					.getWay()
					.getNodeId(
							seg0.isReverse() ? seg0.getWay().getNumberOfNodes() - 1
									: 0);
			long nodeIdN = segN
					.getWay()
					.getNodeId(
							segN.isReverse() ? segN.getWay().getNumberOfNodes() - 1
									: 0);

			OsmNode node1 = resolver.getNode(nodeId1);
			OsmNode nodeN = resolver.getNode(nodeIdN);

			logger.debug("we have an unclosed ring of size " + len);
			logger.debug(String.format("start/end: %f,%f %f,%f",
					node1.getLongitude(), node1.getLatitude(),
					nodeN.getLongitude(), nodeN.getLatitude()));
		}
		return allClosed;
	}

	public static Set<LinearRing> toLinearRings(Collection<SegmentRing> rings,
			OsmEntityProvider resolver) throws EntityNotFoundException
	{
		Set<LinearRing> linearRings = new HashSet<LinearRing>();
		List<SegmentRing> fixedRings = new ArrayList<>();
		for (SegmentRing ring : rings) {
			if (!ring.hasEnoughSegments()) {
				continue;
			}
			if (!ring.hasNodeIntersections()) {
				if (!ring.isClosed()) {
					continue;
				}
				fixedRings.add(ring);
			} else {
				logger.debug("has node intersections");
				List<SegmentRing> newRings = ring.resolveNodeIntersections();
				fixedRings.addAll(newRings);
			}
		}
		for (SegmentRing ring : fixedRings) {
			if (!ring.isClosed() || !ring.hasEnoughSegments()) {
				continue;
			}
			LinearRing linearRing = ring.toLinearRing(resolver);
			linearRings.add(linearRing);
		}
		return linearRings;
	}

	public static List<SegmentRing> convertToSegmentRings(List<WayRing> rings)
	{
		List<SegmentRing> segmentRings = new ArrayList<>();
		for (WayRing ring : rings) {
			segmentRings.add(ring.toSegmentRing());
		}
		return segmentRings;
	}

	public static List<SegmentRing> fixNodeIntersections(List<SegmentRing> rings)
	{
		List<SegmentRing> results = new ArrayList<>();
		for (SegmentRing ring : rings) {
			results.addAll(fixNodeIntersections(ring));
		}
		return results;
	}

	private static List<SegmentRing> fixNodeIntersections(SegmentRing ring)
	{
		List<SegmentRing> results = new ArrayList<>();
		results.add(ring);
		return results;
	}

	public static void closeUnclosedRingWithStraightLine(
			Collection<WayRing> rings)
	{
		for (WayRing ring : rings) {
			if (!ring.isClosed()) {
				logger.debug("unclosed ring with " + ring.getSegments().size()
						+ " segments");
				List<WayRingSegment> segments = ring.getSegments();
				WayRingSegment rs1 = segments.get(0);
				WayRingSegment rs2 = segments.get(segments.size() - 1);
				long n1 = rs1.getWay().getNodeId(
						rs1.isReverse() ? rs1.getWay().getNumberOfNodes() - 1
								: 0);
				long n2 = rs2.getWay().getNodeId(
						rs2.isReverse() ? 0
								: rs2.getWay().getNumberOfNodes() - 1);
				TLongArrayList ids = new TLongArrayList();
				ids.add(n1);
				ids.add(n2);
				OsmWay filler = new Way(0L, ids);
				ring.addWay(filler);
			}
		}
	}

}

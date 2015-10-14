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

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

public class SegmentRing
{

	private TLongList nodeIds;

	public SegmentRing(TLongList nodeIds)
	{
		this.nodeIds = nodeIds;
	}

	public TLongList getNodes()
	{
		return nodeIds;
	}

	public boolean isClosed()
	{
		if (nodeIds.isEmpty()) {
			return true;
		}
		int size = nodeIds.size();
		return nodeIds.get(0) == nodeIds.get(size - 1);
	}

	public boolean hasEnoughSegments()
	{
		int size = nodeIds.size();
		return size >= 4;
	}

	public LinearRing toLinearRing(OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		GeometryFactory factory = new GeometryFactory();
		CoordinateSequenceFactory csf = factory.getCoordinateSequenceFactory();

		int len = nodeIds.size();
		if (len < 4) {
			return new LinearRing(null, factory);
		}
		CoordinateSequence points = csf.create(len, 2);

		for (int i = 0; i < len; i++) {
			OsmNode node = resolver.getNode(nodeIds.get(i));
			points.setOrdinate(i, 0, node.getLongitude());
			points.setOrdinate(i, 1, node.getLatitude());
		}

		LinearRing shell = new LinearRing(points, factory);
		return shell;
	}

	/**
	 * Return whether this ring has intersections in terms of node ids appearing
	 * multiple times.
	 */
	public boolean hasNodeIntersections()
	{
		int size = nodeIds.size();
		// Keep a set of already encountered ids
		TLongSet before = new TLongHashSet();
		// The first id can't be there already because the set is empty
		before.add(nodeIds.get(0));
		// Check all nodes except the last one which gets special care
		for (int i = 1; i < size - 1; i++) {
			long id = nodeIds.get(i);
			// Is it already on the set -> there is an intersection
			if (before.contains(id)) {
				return true;
			}
			// Add to the set of encountered ids
			before.add(id);
		}
		// If this ring is closed, the last node intersects the first one, but
		// that is okay
		if (isClosed()) {
			return false;
		}
		// If the ring is not closed, the last node might be an intersection
		// with one of the others
		return before.contains(nodeIds.get(size - 1));
	}

	/**
	 * Build a new set of rings by splitting at node intersections.
	 */
	public List<SegmentRing> resolveNodeIntersections()
	{
		List<SegmentRing> results = new ArrayList<>();

		// Walk along the nodes in the ring and put them on the stack. Once an
		// id is encountered that is already on the stack somewhere, we found an
		// intersection. We track back by popping elements off the stack until
		// we reach the previous occurrence of the id, building a ring on the
		// way.
		LongSetStack stack = new LongSetStack();

		int size = nodeIds.size();
		for (int i = 0; i < size; i++) {
			long id = nodeIds.get(i);
			if (!stack.contains(id)) {
				// No intersection, just put on the stack
				stack.push(id);
			} else {
				// Intersection, track back
				TLongList list = new TLongArrayList();
				list.add(id);
				while (true) {
					long popped = stack.pop();
					list.add(popped);
					if (popped == id) {
						break;
					}
				}
				results.add(new SegmentRing(list));
				// We need to push the current id to restore the state of the
				// structures as if the ring we just created had not existed at
				// all.
				stack.push(id);
			}
		}
		// If the original ring was closed, the stack will contain exactly one
		// element, the one that we pushed after finding the last ring. Ignore
		// the last node in this case.
		// If the original ring was not closed, there will be more than one
		// element on the stack. In that case, add a last non-closed ring.
		if (stack.size() > 1) {
			TLongList list = new TLongArrayList();
			while (!stack.isEmpty()) {
				list.add(stack.pop());
			}
			results.add(new SegmentRing(list));
		}
		return results;
	}

}

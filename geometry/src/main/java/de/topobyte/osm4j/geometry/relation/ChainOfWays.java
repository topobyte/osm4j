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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;

/*
 * A structure for building rings consisting of a chain of ways.
 */
public class ChainOfWays
{

	private static GeometryFactory factory = new GeometryFactory();

	private List<WaySegment> segments;
	private Set<OsmWay> waySet;
	private long first;
	private long last;
	private boolean closed = false;

	public ChainOfWays(OsmWay way)
	{
		if (way.getNumberOfNodes() < 2) {
			throw new IllegalArgumentException(
					"Only ways with 2 or more nodes are allowed");
		}
		segments = new ArrayList<>();
		waySet = new HashSet<>();
		segments.add(new WaySegment(way, false));
		waySet.add(way);

		first = way.getNodeId(0);
		last = way.getNodeId(way.getNumberOfNodes() - 1);
		closed = first == last;
	}

	/*
	 * The WayRing is valid, iff it is closed and has either none or at least 4
	 * points (the last one equals the first one).
	 */
	public boolean isValidRing()
	{
		if (!closed) {
			return false;
		}
		return lengthIsZero() || !lengthIsLessThan(4);
	}

	/*
	 * Add a way to the set of ways that make up the ring. Assumption: the way
	 * can be added, i.e. one of its tails equals one of the tails of the global
	 * chain.
	 */
	public void addWay(OsmWay way)
	{
		if (way.getNumberOfNodes() < 2) {
			throw new IllegalArgumentException(
					"Only ways with 2 or more nodes are allowed");
		}
		long id0 = way.getNodeId(0);
		long idN = way.getNodeId(way.getNumberOfNodes() - 1);
		boolean reverse = false;
		if (id0 == last) {
			// System.out.println("case " + 0);
			last = idN;
		} else if (id0 == first) {
			// System.out.println("case " + 1);
			first = idN;
			reverse = true;
		} else if (idN == last) {
			// System.out.println("case " + 2);
			last = id0;
			reverse = true;
		} else if (idN == first) {
			// System.out.println("case " + 3);
			first = id0;
		}
		segments.add(new WaySegment(way, reverse));
		waySet.add(way);
		if (first == last) {
			closed = true;
		}
	}

	public long getFirst()
	{
		return first;
	}

	public long getLast()
	{
		return last;
	}

	public boolean isClosed()
	{
		return closed;
	}

	public List<WaySegment> getSegments()
	{
		return segments;
	}

	public Set<OsmWay> getWaySet()
	{
		return waySet;
	}

	public LineString toLineString(OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		CoordinateSequence points = toCoordinateSequence(resolver);

		LineString string = new LineString(points, factory);
		return string;
	}

	public LinearRing toLinearRing(OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		int len = getLength();
		if (len < 4) {
			return new LinearRing(null, factory);
		}

		CoordinateSequence points = toCoordinateSequence(resolver);

		LinearRing shell = new LinearRing(points, factory);
		return shell;
	}

	public int getLength()
	{
		if (segments.isEmpty()) {
			return 0;
		}
		return getLengthNonEmpty();
	}

	private int getLengthNonEmpty()
	{
		int len = 1;
		for (WaySegment segment : segments) {
			len += segment.getWay().getNumberOfNodes() - 1;
		}
		return len;
	}

	private boolean lengthIsZero()
	{
		return segments.isEmpty();
	}

	private boolean lengthIsLessThan(int maxLen)
	{
		if (segments.isEmpty()) {
			return true;
		}
		int len = 1;
		for (WaySegment segment : segments) {
			len += segment.getWay().getNumberOfNodes() - 1;
			if (len >= maxLen) {
				return false;
			}
		}
		return len < maxLen;
	}

	private CoordinateSequence toCoordinateSequence(OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		CoordinateSequenceFactory csf = factory.getCoordinateSequenceFactory();

		int len = getLength();
		CoordinateSequence points = csf.create(len, 2);

		int n = 0;
		for (int i = 0; i < segments.size(); i++) {
			WaySegment segment = segments.get(i);
			OsmWay way = segment.getWay();
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				if (k > 0 || i == 0) {
					OsmNode node = resolver.getNode(segment.getNodeId(k));
					points.setOrdinate(n, 0, node.getLongitude());
					points.setOrdinate(n, 1, node.getLatitude());
					n++;
				}
			}
		}

		return points;
	}

	public ChainOfNodes toSegmentRing()
	{
		if (segments.isEmpty()) {
			return new ChainOfNodes(new TLongArrayList());
		}

		int len = getLengthNonEmpty();

		TLongList ids = new TLongArrayList(len);

		for (int i = 0; i < segments.size(); i++) {
			WaySegment segment = segments.get(i);
			OsmWay way = segment.getWay();
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				if (k > 0 || i == 0) {
					ids.add(segment.getNodeId(k));
				}
			}
		}

		return new ChainOfNodes(ids);
	}

}

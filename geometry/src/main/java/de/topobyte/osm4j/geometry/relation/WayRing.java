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
public class WayRing
{

	private static GeometryFactory factory = new GeometryFactory();

	private List<WayRingSegment> segments;
	private Set<OsmWay> waySet;
	private long first;
	private long last;
	private boolean closed = false;

	public WayRing(OsmWay way) throws EntityNotFoundException
	{
		segments = new ArrayList<WayRingSegment>();
		waySet = new HashSet<OsmWay>();
		segments.add(new WayRingSegment(way, false));
		waySet.add(way);

		first = way.getNodeId(0);
		last = way.getNodeId(way.getNumberOfNodes() - 1);
		closed = first == last;
	}

	/*
	 * The WayRing is valid, iff it is closed and has either none or at least 4
	 * points (the last one equals the first one).
	 */
	public boolean isValid()
	{
		if (!closed) {
			return false;
		}
		int len = 0;
		for (WayRingSegment segment : segments) {
			len += segment.getWay().getNumberOfNodes();
		}
		return len == 0 || len >= 4;
	}

	/*
	 * Add a way to the set of ways that make up the ring. Assumption: the way
	 * can be added, i.e. one of its tails equals one of the tails of the global
	 * chain.
	 */
	public void addWay(OsmWay way)
	{
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
		segments.add(new WayRingSegment(way, reverse));
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

	public List<WayRingSegment> getSegments()
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

	private int getLength()
	{
		int len = 1;
		for (WayRingSegment segment : segments) {
			len += segment.getWay().getNumberOfNodes() - 1;
		}
		return len;
	}

	private CoordinateSequence toCoordinateSequence(OsmEntityProvider resolver)
			throws EntityNotFoundException
	{
		CoordinateSequenceFactory csf = factory.getCoordinateSequenceFactory();

		int len = getLength();
		CoordinateSequence points = csf.create(len, 2);

		int n = 0;
		for (int i = 0; i < segments.size(); i++) {
			WayRingSegment segment = segments.get(i);
			OsmWay way = segment.getWay();
			int nnodes = way.getNumberOfNodes();
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				if (k > 0 || i == 0) {
					int p = segment.isReverse() ? nnodes - k - 1 : k;
					OsmNode node = resolver.getNode(way.getNodeId(p));
					points.setOrdinate(n, 0, node.getLongitude());
					points.setOrdinate(n, 1, node.getLatitude());
					n++;
				}
			}
		}

		return points;
	}

	public SegmentRing toSegmentRing()
	{
		if (segments.isEmpty()) {
			return new SegmentRing(new TLongArrayList());
		}

		int len = 1;
		for (WayRingSegment segment : segments) {
			len += segment.getWay().getNumberOfNodes() - 1;
		}

		TLongList ids = new TLongArrayList(len);

		for (int i = 0; i < segments.size(); i++) {
			WayRingSegment segment = segments.get(i);
			OsmWay way = segment.getWay();
			int nnodes = way.getNumberOfNodes();
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				if (k > 0 || i == 0) {
					int p = segment.isReverse() ? nnodes - k - 1 : k;
					ids.add(way.getNodeId(p));
				}
			}
		}

		return new SegmentRing(ids);
	}

}

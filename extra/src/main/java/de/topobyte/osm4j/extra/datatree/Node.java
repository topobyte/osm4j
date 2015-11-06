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

package de.topobyte.osm4j.extra.datatree;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class Node
{

	boolean isLeaf = true;

	private Envelope envelope;
	private Node parent;
	private Node left;
	private Node right;
	private long path;
	private int level;

	private Direction direction;
	private double splitPoint = 0;

	private Geometry box;

	Node(Envelope envelope, Node parent, long path, int level)
	{
		this.envelope = envelope;
		this.parent = parent;
		this.path = path;
		this.level = level;

		if (envelope.getWidth() >= envelope.getHeight()) {
			direction = Direction.HORIZONTAL;
		} else {
			direction = Direction.VERTICAL;
		}

		this.box = new GeometryFactory().toGeometry(envelope);
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}

	public Node getParent()
	{
		return parent;
	}

	public Node getSibling()
	{
		if (parent == null) {
			return null;
		}
		if (parent.getLeft() != this) {
			return parent.getLeft();
		} else {
			return parent.getRight();
		}
	}

	public long getPath()
	{
		return path;
	}

	public int getLevel()
	{
		return level;
	}

	public boolean isLeaf()
	{
		return isLeaf;
	}

	public Node getLeft()
	{
		return left;
	}

	public Node getRight()
	{
		return right;
	}

	public Direction getSplitDirection()
	{
		return direction;
	}

	public void melt()
	{
		isLeaf = true;
		left = null;
		right = null;
	}

	public void split()
	{
		Direction direction = getSplitDirection();
		long pathL = path << 1 | 0;
		long pathR = path << 1 | 1;
		Envelope envLeft, envRight;
		if (direction == Direction.HORIZONTAL) {
			double x1 = envelope.getMinX();
			double x3 = envelope.getMaxX();
			double x2 = (x1 + x3) / 2;
			double y1 = envelope.getMinY();
			double y2 = envelope.getMaxY();
			envLeft = new Envelope(x1, x2, y1, y2);
			envRight = new Envelope(x2, x3, y1, y2);
			splitPoint = x2;
		} else {
			double x1 = envelope.getMinX();
			double x2 = envelope.getMaxX();
			double y1 = envelope.getMinY();
			double y3 = envelope.getMaxY();
			double y2 = (y1 + y3) / 2;
			envLeft = new Envelope(x1, x2, y1, y2);
			envRight = new Envelope(x1, x2, y2, y3);
			splitPoint = y2;
		}
		isLeaf = false;
		left = new Node(envLeft, this, pathL, level + 1);
		right = new Node(envRight, this, pathR, level + 1);
	}

	public void split(int depth)
	{
		if (depth >= 1) {
			split();
		}
		if (depth > 1) {
			left.split(depth - 1);
			right.split(depth - 1);
		}
	}

	public void query(List<Node> nodes, double lon, double lat)
	{
		if (isLeaf()) {
			nodes.add(this);
			return;
		}

		if (direction == Direction.HORIZONTAL) {
			if (lon < splitPoint) {
				left.query(nodes, lon, lat);
			} else if (lon > splitPoint) {
				right.query(nodes, lon, lat);
			} else {
				left.query(nodes, lon, lat);
				right.query(nodes, lon, lat);
			}
		} else {
			if (lat < splitPoint) {
				left.query(nodes, lon, lat);
			} else if (lat > splitPoint) {
				right.query(nodes, lon, lat);
			} else {
				left.query(nodes, lon, lat);
				right.query(nodes, lon, lat);
			}
		}
	}

	public Side side(double lon, double lat)
	{
		if (direction == Direction.HORIZONTAL) {
			if (lon < splitPoint) {
				return Side.LEFT;
			} else if (lon > splitPoint) {
				return Side.RIGHT;
			} else {
				return Side.ON;
			}
		} else {
			if (lat < splitPoint) {
				return Side.LEFT;
			} else if (lat > splitPoint) {
				return Side.RIGHT;
			} else {
				return Side.ON;
			}
		}
	}

	public void query(List<Node> nodes, Geometry geometry)
	{
		if (!box.intersects(geometry)) {
			return;
		}

		if (isLeaf()) {
			nodes.add(this);
			return;
		}

		left.query(nodes, geometry);
		right.query(nodes, geometry);
	}

}

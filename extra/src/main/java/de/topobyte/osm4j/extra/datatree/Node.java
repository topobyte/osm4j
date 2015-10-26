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

public class Node
{

	private Envelope envelope;
	private Node parent;
	private Node left;
	private Node right;
	private int path;
	private Direction direction;

	Node(Envelope envelope, Node parent, int path)
	{
		this.envelope = envelope;
		this.parent = parent;
		this.path = path;

		if (envelope.getWidth() >= envelope.getHeight()) {
			direction = Direction.HORIZONTAL;
		} else {
			direction = Direction.VERTICAL;
		}
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}

	public Node getParent()
	{
		return parent;
	}

	public int getPath()
	{
		return path;
	}

	public boolean isLeaf()
	{
		return left == null && right == null;
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

	public void split()
	{
		Direction direction = getSplitDirection();
		int pathL = path << 1 | 0;
		int pathR = path << 1 | 1;
		Envelope envLeft, envRight;
		if (direction == Direction.HORIZONTAL) {
			double x1 = envelope.getMinX();
			double x3 = envelope.getMaxX();
			double x2 = (x1 + x3) / 2;
			double y1 = envelope.getMinY();
			double y2 = envelope.getMaxY();
			envLeft = new Envelope(x1, x2, y1, y2);
			envRight = new Envelope(x2, x3, y1, y2);
		} else {
			double x1 = envelope.getMinX();
			double x2 = envelope.getMaxX();
			double y1 = envelope.getMinY();
			double y3 = envelope.getMaxY();
			double y2 = (y1 + y3) / 2;
			envLeft = new Envelope(x1, x2, y1, y2);
			envRight = new Envelope(x1, x2, y2, y3);
		}
		left = new Node(envLeft, this, pathL);
		right = new Node(envRight, this, pathR);
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
		if (!envelope.contains(lon, lat)) {
			return;
		}
		if (isLeaf()) {
			nodes.add(this);
		} else {
			left.query(nodes, lon, lat);
			right.query(nodes, lon, lat);
		}
	}

}

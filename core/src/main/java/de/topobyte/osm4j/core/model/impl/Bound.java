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

package de.topobyte.osm4j.core.model.impl;

public class Bound
{

	private final double left;
	private final double right;
	private final double top;
	private final double bottom;

	public Bound(double left, double right, double top, double bottom)
	{
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
	}

	public double getLeft()
	{
		return left;
	}

	public double getRight()
	{
		return right;
	}

	public double getTop()
	{
		return top;
	}

	public double getBottom()
	{
		return bottom;
	}

}

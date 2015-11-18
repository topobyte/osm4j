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

package de.topobyte.osm4j.extra.query;

public class QueryResult
{

	private int nNodes;
	private int nWays;
	private int nSimpleRelations;
	private int nComplexRelations;

	public QueryResult(int nNodes, int nWays, int nSimpleRelations,
			int nComplexRelations)
	{
		this.nNodes = nNodes;
		this.nWays = nWays;
		this.nSimpleRelations = nSimpleRelations;
		this.nComplexRelations = nComplexRelations;
	}

	public int getNumNodes()
	{
		return nNodes;
	}

	public int getNumWays()
	{
		return nWays;
	}

	public int getNumSimpleRelations()
	{
		return nSimpleRelations;
	}

	public int getNumComplexRelations()
	{
		return nComplexRelations;
	}

}

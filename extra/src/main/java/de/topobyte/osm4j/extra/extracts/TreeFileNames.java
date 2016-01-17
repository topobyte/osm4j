// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.extracts;

import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;

public class TreeFileNames
{

	private String nodes;
	private String ways;
	private String simpleRelations;
	private String complexRelations;

	public TreeFileNames(FileFormat outputFormat)
	{
		String extension = OsmIoUtils.extension(outputFormat);

		nodes = "nodes" + extension;
		ways = "ways" + extension;
		simpleRelations = "relations.simple" + extension;
		complexRelations = "relations.complex" + extension;
	}

	public String getNodes()
	{
		return nodes;
	}

	public void setNodes(String nodes)
	{
		this.nodes = nodes;
	}

	public String getWays()
	{
		return ways;
	}

	public void setWays(String ways)
	{
		this.ways = ways;
	}

	public String getSimpleRelations()
	{
		return simpleRelations;
	}

	public void setSimpleRelations(String simpleRelations)
	{
		this.simpleRelations = simpleRelations;
	}

	public String getComplexRelations()
	{
		return complexRelations;
	}

	public void setComplexRelations(String complexRelations)
	{
		this.complexRelations = complexRelations;
	}

}

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

import java.nio.file.Path;

public class ExtractionPaths
{

	private Path splitNodes;
	private Path splitWays;
	private Path splitRelations;

	private Path tree;
	private Path waysByNodes;

	private Path simpleRelations;
	private Path complexRelations;

	private Path simpleRelationsBboxes;
	private Path complexRelationsBboxes;

	private Path simpleRelationsEmpty;
	private Path complexRelationsEmpty;

	public ExtractionPaths(Path base, ExtractionFileNames fileNames)
	{
		splitNodes = base.resolve(fileNames.getSplitNodes());
		splitWays = base.resolve(fileNames.getSplitWays());
		splitRelations = base.resolve(fileNames.getSplitRelations());

		simpleRelations = base.resolve(fileNames.getSimpleRelations());
		complexRelations = base.resolve(fileNames.getComplexRelations());

		simpleRelationsBboxes = base.resolve(fileNames
				.getSimpleRelationsBboxes());
		complexRelationsBboxes = base.resolve(fileNames
				.getComplexRelationsBboxes());

		simpleRelationsEmpty = base
				.resolve(fileNames.getSimpleRelationsEmpty());
		complexRelationsEmpty = base.resolve(fileNames
				.getComplexRelationsEmpty());
	}

	public Path getSplitNodes()
	{
		return splitNodes;
	}

	public void setSplitNodes(Path splitNodes)
	{
		this.splitNodes = splitNodes;
	}

	public Path getSplitWays()
	{
		return splitWays;
	}

	public void setSplitWays(Path splitWays)
	{
		this.splitWays = splitWays;
	}

	public Path getSplitRelations()
	{
		return splitRelations;
	}

	public void setSplitRelations(Path splitRelations)
	{
		this.splitRelations = splitRelations;
	}

	public Path getTree()
	{
		return tree;
	}

	public void setTree(Path tree)
	{
		this.tree = tree;
	}

	public Path getWaysByNodes()
	{
		return waysByNodes;
	}

	public void setWaysByNodes(Path waysByNodes)
	{
		this.waysByNodes = waysByNodes;
	}

	public Path getSimpleRelations()
	{
		return simpleRelations;
	}

	public void setSimpleRelations(Path simpleRelations)
	{
		this.simpleRelations = simpleRelations;
	}

	public Path getComplexRelations()
	{
		return complexRelations;
	}

	public void setComplexRelations(Path complexRelations)
	{
		this.complexRelations = complexRelations;
	}

	public Path getSimpleRelationsBboxes()
	{
		return simpleRelationsBboxes;
	}

	public void setSimpleRelationsBboxes(Path simpleRelationsBboxes)
	{
		this.simpleRelationsBboxes = simpleRelationsBboxes;
	}

	public Path getComplexRelationsBboxes()
	{
		return complexRelationsBboxes;
	}

	public void setComplexRelationsBboxes(Path complexRelationsBboxes)
	{
		this.complexRelationsBboxes = complexRelationsBboxes;
	}

	public Path getSimpleRelationsEmpty()
	{
		return simpleRelationsEmpty;
	}

	public void setSimpleRelationsEmpty(Path simpleRelationsEmpty)
	{
		this.simpleRelationsEmpty = simpleRelationsEmpty;
	}

	public Path getComplexRelationsEmpty()
	{
		return complexRelationsEmpty;
	}

	public void setComplexRelationsEmpty(Path complexRelationsEmpty)
	{
		this.complexRelationsEmpty = complexRelationsEmpty;
	}

}

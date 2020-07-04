// Copyright 2020 Sebastian Kuerten
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

public class ExtractionFiles
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

	public ExtractionFiles(Path path, ExtractionFileNames filenames)
	{
		splitNodes = path.resolve(filenames.getSplitNodes());
		splitWays = path.resolve(filenames.getSplitWays());
		splitRelations = path.resolve(filenames.getSplitRelations());

		tree = path.resolve(filenames.getTree());
		waysByNodes = path.resolve(filenames.getWaysByNodes());

		simpleRelations = path.resolve(filenames.getSimpleRelations());
		complexRelations = path.resolve(filenames.getComplexRelations());

		simpleRelationsBboxes = path
				.resolve(filenames.getSimpleRelationsBboxes());
		complexRelationsBboxes = path
				.resolve(filenames.getComplexRelationsBboxes());

		simpleRelationsEmpty = path
				.resolve(filenames.getSimpleRelationsEmpty());
		complexRelationsEmpty = path
				.resolve(filenames.getComplexRelationsEmpty());
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

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

public class ExtractionFileNames
{

	private String splitNodes;
	private String splitWays;
	private String splitRelations;

	private String tree = "tree";
	private String waysByNodes = "waysbynodes";

	private String simpleRelations = "relations.simple.sorted";
	private String complexRelations = "relations.complex.sorted";

	private String simpleRelationsBboxes = "relations.simple.sorted.bboxlist";
	private String complexRelationsBboxes = "relations.complex.sorted.bboxlist";

	private String simpleRelationsEmpty;
	private String complexRelationsEmpty;

	private TreeFileNames treeNames;
	private BatchFileNames relationNames;

	public ExtractionFileNames(FileFormat outputFormat)
	{
		String extension = OsmIoUtils.extension(outputFormat);

		splitNodes = "nodes" + extension;
		splitWays = "ways" + extension;
		splitRelations = "relations" + extension;

		treeNames = new TreeFileNames(outputFormat);
		relationNames = new BatchFileNames(outputFormat);

		simpleRelationsEmpty = "relations.simple.empty" + extension;
		complexRelationsEmpty = "relations.complex.empty" + extension;
	}

	public String getSplitNodes()
	{
		return splitNodes;
	}

	public void setSplitNodes(String splitNodes)
	{
		this.splitNodes = splitNodes;
	}

	public String getSplitWays()
	{
		return splitWays;
	}

	public void setSplitWays(String splitWays)
	{
		this.splitWays = splitWays;
	}

	public String getSplitRelations()
	{
		return splitRelations;
	}

	public void setSplitRelations(String splitRelations)
	{
		this.splitRelations = splitRelations;
	}

	public String getTree()
	{
		return tree;
	}

	public void setTree(String tree)
	{
		this.tree = tree;
	}

	public String getWaysByNodes()
	{
		return waysByNodes;
	}

	public void setWaysByNodes(String waysByNodes)
	{
		this.waysByNodes = waysByNodes;
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

	public String getSimpleRelationsBboxes()
	{
		return simpleRelationsBboxes;
	}

	public void setSimpleRelationsBboxes(String simpleRelationsBboxes)
	{
		this.simpleRelationsBboxes = simpleRelationsBboxes;
	}

	public String getComplexRelationsBboxes()
	{
		return complexRelationsBboxes;
	}

	public void setComplexRelationsBboxes(String complexRelationsBboxes)
	{
		this.complexRelationsBboxes = complexRelationsBboxes;
	}

	public String getSimpleRelationsEmpty()
	{
		return simpleRelationsEmpty;
	}

	public void setSimpleRelationsEmpty(String simpleRelationsEmpty)
	{
		this.simpleRelationsEmpty = simpleRelationsEmpty;
	}

	public String getComplexRelationsEmpty()
	{
		return complexRelationsEmpty;
	}

	public void setComplexRelationsEmpty(String complexRelationsEmpty)
	{
		this.complexRelationsEmpty = complexRelationsEmpty;
	}

	public TreeFileNames getTreeNames()
	{
		return treeNames;
	}

	public void setTreeNames(TreeFileNames treeNames)
	{
		this.treeNames = treeNames;
	}

	public BatchFileNames getRelationNames()
	{
		return relationNames;
	}

	public void setRelationNames(BatchFileNames relationNames)
	{
		this.relationNames = relationNames;
	}

}

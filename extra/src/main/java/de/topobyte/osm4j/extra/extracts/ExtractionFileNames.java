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

	private String treeNodes;
	private String treeWays;
	private String treeSimpleRelations;
	private String treeComplexRelations;

	private String relationNodes;
	private String relationWays;
	private String relationRelations;

	public ExtractionFileNames(FileFormat outputFormat)
	{
		String extension = OsmIoUtils.extension(outputFormat);

		splitNodes = "nodes" + extension;
		splitWays = "ways" + extension;
		splitRelations = "relations" + extension;

		treeNodes = "nodes" + extension;
		treeWays = "ways" + extension;
		treeSimpleRelations = "relations.simple" + extension;
		treeComplexRelations = "relations.complex" + extension;

		relationNodes = "nodes" + extension;
		relationWays = "ways" + extension;
		relationRelations = "relations" + extension;

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

	public String getTreeNodes()
	{
		return treeNodes;
	}

	public void setTreeNodes(String treeNodes)
	{
		this.treeNodes = treeNodes;
	}

	public String getTreeWays()
	{
		return treeWays;
	}

	public void setTreeWays(String treeWays)
	{
		this.treeWays = treeWays;
	}

	public String getTreeSimpleRelations()
	{
		return treeSimpleRelations;
	}

	public void setTreeSimpleRelations(String treeSimpleRelations)
	{
		this.treeSimpleRelations = treeSimpleRelations;
	}

	public String getTreeComplexRelations()
	{
		return treeComplexRelations;
	}

	public void setTreeComplexRelations(String treeComplexRelations)
	{
		this.treeComplexRelations = treeComplexRelations;
	}

	public String getRelationNodes()
	{
		return relationNodes;
	}

	public void setRelationNodes(String relationNodes)
	{
		this.relationNodes = relationNodes;
	}

	public String getRelationWays()
	{
		return relationWays;
	}

	public void setRelationWays(String relationWays)
	{
		this.relationWays = relationWays;
	}

	public String getRelationRelations()
	{
		return relationRelations;
	}

	public void setRelationRelations(String relationRelations)
	{
		this.relationRelations = relationRelations;
	}

}

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

	public final String SPLIT_NODES;
	public final String SPLIT_WAYS;
	public final String SPLIT_RELATIONS;

	public final String TREE = "tree";
	public final String WAYS_BY_NODES = "waysbynodes";

	public final String SIMPLE_RELATIONS = "relations.simple.sorted";
	public final String COMPLEX_RELATIONS = "relations.complex.sorted";

	public final String SIMPLE_RELATIONS_BBOXES = "relations.simple.sorted.bboxlist";
	public final String COMPLEX_RELATIONS_BBOXES = "relations.complex.sorted.bboxlist";

	public final String SIMPLE_RELATIONS_EMPTY;
	public final String COMPLEX_RELATIONS_EMPTY;

	public final String TREE_NODES;
	public final String TREE_WAYS;
	public final String TREE_SIMPLE_RELATIONS;
	public final String TREE_COMPLEX_RELATIONS;

	public final String RELATION_NODES;
	public final String RELATION_WAYS;
	public final String RELATION_RELATIONS;

	public ExtractionFileNames(FileFormat outputFormat)
	{
		String extension = OsmIoUtils.extension(outputFormat);

		SPLIT_NODES = "nodes" + extension;
		SPLIT_WAYS = "ways" + extension;
		SPLIT_RELATIONS = "relations" + extension;

		TREE_NODES = "nodes" + extension;
		TREE_WAYS = "ways" + extension;
		TREE_SIMPLE_RELATIONS = "relations.simple" + extension;
		TREE_COMPLEX_RELATIONS = "relations.complex" + extension;

		RELATION_NODES = "nodes" + extension;
		RELATION_WAYS = "ways" + extension;
		RELATION_RELATIONS = "relations" + extension;

		SIMPLE_RELATIONS_EMPTY = "relations.simple.empty" + extension;
		COMPLEX_RELATIONS_EMPTY = "relations.complex.empty" + extension;
	}

}

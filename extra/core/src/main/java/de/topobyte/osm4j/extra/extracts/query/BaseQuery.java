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

package de.topobyte.osm4j.extra.extracts.query;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;

import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.extracts.BatchFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionPaths;
import de.topobyte.osm4j.extra.extracts.TreeFileNames;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class BaseQuery extends AbstractQuery
{

	protected GeometryFactory factory = new GeometryFactory();

	protected ExtractionPaths paths;
	protected TreeFileNames treeNames;
	protected BatchFileNames relationNames;

	protected DataTree tree;
	protected DataTreeFiles filesTreeNodes;
	protected DataTreeFiles filesTreeWays;
	protected DataTreeFiles filesTreeSimpleRelations;
	protected DataTreeFiles filesTreeComplexRelations;

	public BaseQuery(ExtractionPaths paths, TreeFileNames treeNames,
			BatchFileNames relationNames, FileFormat inputFormat,
			OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig)
	{
		super(inputFormat, outputConfigIntermediate, outputConfig);

		this.paths = paths;
		this.treeNames = treeNames;
		this.relationNames = relationNames;
	}

	protected void openTree() throws IOException
	{
		Path pathTree = paths.getTree();
		tree = DataTreeOpener.open(pathTree);

		filesTreeNodes = new DataTreeFiles(pathTree, treeNames.getNodes());
		filesTreeWays = new DataTreeFiles(pathTree, treeNames.getWays());
		filesTreeSimpleRelations = new DataTreeFiles(pathTree,
				treeNames.getSimpleRelations());
		filesTreeComplexRelations = new DataTreeFiles(pathTree,
				treeNames.getComplexRelations());
	}

	protected OsmFileInput input(Path path)
	{
		return new OsmFileInput(path, inputFormat);
	}

	protected OsmFileInput intermediate(Path path)
	{
		return new OsmFileInput(path, outputConfigIntermediate.getFileFormat());
	}

	protected void addCompletelyContainedLeaf(IntermediateFiles files,
			Node leaf)
	{
		files.getFilesNodes().add(input(filesTreeNodes.getPath(leaf)));
		files.getFilesWays().add(input(filesTreeWays.getPath(leaf)));
		files.getFilesSimpleRelations()
				.add(input(filesTreeSimpleRelations.getPath(leaf)));
		files.getFilesComplexRelations()
				.add(input(filesTreeComplexRelations.getPath(leaf)));
	}

	protected void addCompletelyContainedBatch(IntermediateFiles files,
			Path pathRelations, long id, List<OsmFileInput> filesRelations)
	{
		Path path = pathRelations.resolve(Long.toString(id));
		files.getFilesNodes()
				.add(input(path.resolve(relationNames.getNodes())));
		files.getFilesWays().add(input(path.resolve(relationNames.getWays())));
		filesRelations.add(input(path.resolve(relationNames.getRelations())));
	}

}

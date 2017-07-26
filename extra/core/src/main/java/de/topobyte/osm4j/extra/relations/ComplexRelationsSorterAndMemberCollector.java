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

package de.topobyte.osm4j.extra.relations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.extra.relations.split.ComplexRelationSorter;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class ComplexRelationsSorterAndMemberCollector
{

	private int maxMembers;

	private OsmIteratorInputFactory inputComplexRelations;

	private Path pathInputComplexRelationsBboxes;

	private OsmIteratorInputFactory inputWays;
	private OsmIteratorInputFactory inputNodes;

	private Path pathOutputComplexRelations;
	private String fileNamesRelationsUnsorted;
	private String fileNamesRelations;

	private boolean keepUnsortedRelations;

	private OsmOutputConfig outputConfig;

	private Path pathOutputBboxList;

	public ComplexRelationsSorterAndMemberCollector(
			OsmIteratorInputFactory inputComplexRelations,
			Path pathInputComplexRelationsBboxes,
			Path pathOutputComplexRelations, String fileNamesRelationsUnsorted,
			String fileNamesRelations, OsmIteratorInputFactory inputWays,
			OsmIteratorInputFactory inputNodes, OsmOutputConfig outputConfig,
			Path pathOutputBboxList, int maxMembers,
			boolean keepUnsortedRelations)
	{
		this.inputComplexRelations = inputComplexRelations;
		this.pathInputComplexRelationsBboxes = pathInputComplexRelationsBboxes;
		this.pathOutputComplexRelations = pathOutputComplexRelations;
		this.fileNamesRelationsUnsorted = fileNamesRelationsUnsorted;
		this.fileNamesRelations = fileNamesRelations;
		this.inputWays = inputWays;
		this.inputNodes = inputNodes;
		this.outputConfig = outputConfig;
		this.pathOutputBboxList = pathOutputBboxList;
		this.maxMembers = maxMembers;
		this.keepUnsortedRelations = keepUnsortedRelations;
	}

	public void execute() throws IOException
	{
		// Create output directories

		Files.createDirectories(pathOutputComplexRelations);

		// Split relations into sorted batches

		ComplexRelationSorter complexRelationSorter = new ComplexRelationSorter(
				pathInputComplexRelationsBboxes, pathOutputComplexRelations,
				fileNamesRelationsUnsorted, fileNamesRelations,
				inputComplexRelations, outputConfig, pathOutputBboxList,
				maxMembers, keepUnsortedRelations);

		complexRelationSorter.execute();

		// Collect relations' members

		List<Path> pathsRelations = new ArrayList<>();
		pathsRelations.add(pathOutputComplexRelations);

		RelationsMemberCollector memberCollector = new RelationsMemberCollector(
				pathsRelations, fileNamesRelations, inputWays, inputNodes,
				outputConfig);
		memberCollector.execute();
	}

}

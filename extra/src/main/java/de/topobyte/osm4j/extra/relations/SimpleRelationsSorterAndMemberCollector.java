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
import de.topobyte.osm4j.extra.relations.split.SimpleRelationSorter;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class SimpleRelationsSorterAndMemberCollector
{

	private OsmIteratorInputFactory inputSimpleRelations;

	private Path pathInputSimpleRelationsBboxes;

	private OsmIteratorInputFactory inputWays;
	private OsmIteratorInputFactory inputNodes;

	private Path pathOutputSimpleRelations;
	private String fileNamesRelations;

	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	public SimpleRelationsSorterAndMemberCollector(
			OsmIteratorInputFactory inputSimpleRelations,
			Path pathInputSimpleRelationsBboxes,
			Path pathOutputSimpleRelations, String fileNamesRelations,
			OsmIteratorInputFactory inputWays,
			OsmIteratorInputFactory inputNodes, FileFormat outputFormat,
			boolean writeMetadata, PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.inputSimpleRelations = inputSimpleRelations;
		this.pathInputSimpleRelationsBboxes = pathInputSimpleRelationsBboxes;
		this.pathOutputSimpleRelations = pathOutputSimpleRelations;
		this.fileNamesRelations = fileNamesRelations;
		this.inputWays = inputWays;
		this.inputNodes = inputNodes;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
	}

	public void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		// Create output directories

		Files.createDirectories(pathOutputSimpleRelations);

		// Split relations into sorted batches

		SimpleRelationSorter simpleRelationSorter = new SimpleRelationSorter(
				pathInputSimpleRelationsBboxes, pathOutputSimpleRelations,
				fileNamesRelations, inputSimpleRelations, outputConfig);

		simpleRelationSorter.execute();

		// Collect relations' members

		List<Path> pathsRelations = new ArrayList<>();
		pathsRelations.add(pathOutputSimpleRelations);

		RelationsMemberCollector memberCollector = new RelationsMemberCollector(
				pathsRelations, fileNamesRelations, inputWays, inputNodes,
				outputConfig);
		memberCollector.execute();
	}

}

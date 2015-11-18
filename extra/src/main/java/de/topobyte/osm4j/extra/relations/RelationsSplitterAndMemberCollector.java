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
import de.topobyte.osm4j.extra.relations.split.ComplexRelationSplitter;
import de.topobyte.osm4j.extra.relations.split.SimpleRelationSplitter;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class RelationsSplitterAndMemberCollector
{

	private OsmIteratorInputFactory inputSimpleRelations;
	private OsmIteratorInputFactory inputComplexRelations;

	private OsmIteratorInputFactory inputWays;
	private OsmIteratorInputFactory inputNodes;

	private Path pathOutputSimpleRelations;
	private Path pathOutputComplexRelations;
	private String fileNamesRelations;

	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	public RelationsSplitterAndMemberCollector(
			OsmIteratorInputFactory inputSimpleRelations,
			OsmIteratorInputFactory inputComplexRelations,
			Path pathOutputSimpleRelations, Path pathOutputComplexRelations,
			String fileNamesRelations, OsmIteratorInputFactory inputWays,
			OsmIteratorInputFactory inputNodes, FileFormat outputFormat,
			boolean writeMetadata, PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.inputSimpleRelations = inputSimpleRelations;
		this.inputComplexRelations = inputComplexRelations;
		this.pathOutputSimpleRelations = pathOutputSimpleRelations;
		this.pathOutputComplexRelations = pathOutputComplexRelations;
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
		// Create output directories

		Files.createDirectories(pathOutputSimpleRelations);
		Files.createDirectories(pathOutputComplexRelations);

		// Split relations into batches

		SimpleRelationSplitter simpleRelationSplitter = new SimpleRelationSplitter(
				pathOutputSimpleRelations, fileNamesRelations,
				inputSimpleRelations, outputFormat, writeMetadata, pbfConfig,
				tboConfig);

		simpleRelationSplitter.execute();

		ComplexRelationSplitter complexRelationSplitter = new ComplexRelationSplitter(
				pathOutputComplexRelations, fileNamesRelations,
				inputComplexRelations, outputFormat, writeMetadata, pbfConfig,
				tboConfig);

		complexRelationSplitter.execute();

		// Collect members

		List<Path> pathsRelations = new ArrayList<>();
		pathsRelations.add(pathOutputSimpleRelations);
		pathsRelations.add(pathOutputComplexRelations);

		RelationsMemberCollector memberCollector = new RelationsMemberCollector(
				pathsRelations, fileNamesRelations, inputWays, inputNodes,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
		memberCollector.execute();
	}
}

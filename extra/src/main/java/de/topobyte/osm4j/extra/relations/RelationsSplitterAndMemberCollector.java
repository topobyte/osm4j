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

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.extra.idextract.ExtractionItem;
import de.topobyte.osm4j.extra.idextract.ExtractionUtil;
import de.topobyte.osm4j.extra.idextract.Extractor;
import de.topobyte.osm4j.extra.relations.split.ComplexRelationSplitter;
import de.topobyte.osm4j.extra.relations.split.SimpleRelationSplitter;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmIteratorFactory;
import de.topobyte.osm4j.utils.OsmIteratorInput;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class RelationsSplitterAndMemberCollector
{

	private OsmIteratorFactory inputSimpleRelations;
	private OsmIteratorFactory inputComplexRelations;

	private OsmIteratorFactory inputWays;
	private OsmIteratorFactory inputNodes;

	private Path pathOutputSimpleRelations;
	private Path pathOutputComplexRelations;
	private String fileNamesRelations;

	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	public RelationsSplitterAndMemberCollector(
			OsmIteratorFactory inputSimpleRelations,
			OsmIteratorFactory inputComplexRelations,
			Path pathOutputSimpleRelations, Path pathOutputComplexRelations,
			String fileNamesRelations, OsmIteratorFactory inputWays,
			OsmIteratorFactory inputNodes, FileFormat outputFormat,
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
		String fileNamesRelationNodeIds = "nodes.ids";
		String fileNamesRelationWayIds = "ways.ids";
		String fileNamesWayNodeIds = "waynodes.ids";
		String fileNamesWays = "ways" + OsmIoUtils.extension(outputFormat);
		String fileNamesNodes = "nodes" + OsmIoUtils.extension(outputFormat);

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

		// Extract relation member ids for each batch

		Path[] dirsData = new Path[] { pathOutputSimpleRelations,
				pathOutputComplexRelations };

		MemberIdsExtractor memberIdsExtractor = new MemberIdsExtractor(
				dirsData, fileNamesRelations, fileNamesRelationNodeIds,
				fileNamesRelationWayIds, outputFormat);
		memberIdsExtractor.execute();

		// Extract ways for each batch

		List<ExtractionItem> wayExtractionItems = new ArrayList<>();
		wayExtractionItems.addAll(ExtractionUtil.createExtractionItems(
				pathOutputSimpleRelations, fileNamesRelationWayIds,
				fileNamesWays));
		wayExtractionItems.addAll(ExtractionUtil.createExtractionItems(
				pathOutputComplexRelations, fileNamesRelationWayIds,
				fileNamesWays));

		Extractor wayExtractor = new Extractor(EntityType.Way,
				wayExtractionItems, outputFormat, pbfConfig, tboConfig,
				writeMetadata);
		OsmIteratorInput wayInput = inputWays.createIterator(writeMetadata);
		wayExtractor.execute(wayInput.getIterator());
		wayInput.close();

		// Extract way node ids for each batch

		WayMemberNodeIdsExtractor wayMemberNodeIdsExtractor = new WayMemberNodeIdsExtractor(
				dirsData, fileNamesWays, fileNamesWayNodeIds, outputFormat);
		wayMemberNodeIdsExtractor.execute();

		// Extract nodes for each batch

		String[] fileNamesNodeIds = new String[] { fileNamesRelationNodeIds,
				fileNamesWayNodeIds };

		List<ExtractionItem> nodeExtractionItems = new ArrayList<>();
		nodeExtractionItems.addAll(ExtractionUtil.createExtractionItems(
				pathOutputSimpleRelations, fileNamesNodeIds, fileNamesNodes));
		nodeExtractionItems.addAll(ExtractionUtil.createExtractionItems(
				pathOutputComplexRelations, fileNamesNodeIds, fileNamesNodes));

		Extractor nodeExtractor = new Extractor(EntityType.Node,
				nodeExtractionItems, outputFormat, pbfConfig, tboConfig,
				writeMetadata);
		OsmIteratorInput nodeInput = inputNodes.createIterator(writeMetadata);
		nodeExtractor.execute(nodeInput.getIterator());
		nodeInput.close();
	}

}

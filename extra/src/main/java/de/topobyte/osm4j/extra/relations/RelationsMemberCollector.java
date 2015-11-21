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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.extra.idextract.ExtractionItem;
import de.topobyte.osm4j.extra.idextract.ExtractionUtil;
import de.topobyte.osm4j.extra.idextract.Extractor;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class RelationsMemberCollector
{

	public static final String FILE_NAMES_NODE_IDS = "nodes.ids";
	public static final String FILE_NAMES_WAY_IDS = "ways.ids";
	public static final String FILE_NAMES_WAY_NODE_IDS = "waynodes.ids";
	public static final String FILE_NAMES_NODE_BASENAME = "nodes";
	public static final String FILE_NAMES_WAY_BASENAME = "ways";

	private OsmIteratorInputFactory inputWays;
	private OsmIteratorInputFactory inputNodes;

	private List<Path> pathsRelations;
	private String fileNamesRelations;

	private OsmOutputConfig outputConfig;

	public RelationsMemberCollector(List<Path> pathsRelations,
			String fileNamesRelations, OsmIteratorInputFactory inputWays,
			OsmIteratorInputFactory inputNodes, OsmOutputConfig outputConfig)
	{
		this.pathsRelations = pathsRelations;
		this.fileNamesRelations = fileNamesRelations;
		this.inputWays = inputWays;
		this.inputNodes = inputNodes;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		String fileNamesRelationNodeIds = FILE_NAMES_NODE_IDS;
		String fileNamesRelationWayIds = FILE_NAMES_WAY_IDS;
		String fileNamesWayNodeIds = FILE_NAMES_WAY_NODE_IDS;
		String fileNamesWays = FILE_NAMES_NODE_BASENAME
				+ OsmIoUtils.extension(outputConfig.getFileFormat());
		String fileNamesNodes = FILE_NAMES_WAY_BASENAME
				+ OsmIoUtils.extension(outputConfig.getFileFormat());

		// Extract relation member ids for each batch

		Path[] dirsData = pathsRelations.toArray(new Path[0]);

		MemberIdsExtractor memberIdsExtractor = new MemberIdsExtractor(
				dirsData, fileNamesRelations, fileNamesRelationNodeIds,
				fileNamesRelationWayIds, outputConfig.getFileFormat());
		memberIdsExtractor.execute();

		// Extract ways for each batch

		List<ExtractionItem> wayExtractionItems = new ArrayList<>();
		for (Path path : pathsRelations) {
			wayExtractionItems.addAll(ExtractionUtil.createExtractionItems(
					path, fileNamesRelationWayIds, fileNamesWays));
		}

		Extractor wayExtractor = new Extractor(EntityType.Way,
				wayExtractionItems, outputConfig);
		OsmIteratorInput wayInput = inputWays.createIterator(outputConfig
				.isWriteMetadata());
		wayExtractor.execute(wayInput.getIterator());
		wayInput.close();

		// Extract way node ids for each batch

		WayMemberNodeIdsExtractor wayMemberNodeIdsExtractor = new WayMemberNodeIdsExtractor(
				dirsData, fileNamesWays, fileNamesWayNodeIds,
				outputConfig.getFileFormat());
		wayMemberNodeIdsExtractor.execute();

		// Extract nodes for each batch

		String[] fileNamesNodeIds = new String[] { fileNamesRelationNodeIds,
				fileNamesWayNodeIds };

		List<ExtractionItem> nodeExtractionItems = new ArrayList<>();
		for (Path path : pathsRelations) {
			nodeExtractionItems.addAll(ExtractionUtil.createExtractionItems(
					path, fileNamesNodeIds, fileNamesNodes));
		}

		Extractor nodeExtractor = new Extractor(EntityType.Node,
				nodeExtractionItems, outputConfig);
		OsmIteratorInput nodeInput = inputNodes.createIterator(outputConfig
				.isWriteMetadata());
		nodeExtractor.execute(nodeInput.getIterator());
		nodeInput.close();
	}

}

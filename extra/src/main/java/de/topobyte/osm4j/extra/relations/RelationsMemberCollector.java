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
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class RelationsMemberCollector
{

	private OsmIteratorInputFactory inputWays;
	private OsmIteratorInputFactory inputNodes;

	private List<Path> pathsRelations;
	private String fileNamesRelations;

	private FileFormat outputFormat;
	private boolean writeMetadata;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	public RelationsMemberCollector(List<Path> pathsRelations,
			String fileNamesRelations, OsmIteratorInputFactory inputWays,
			OsmIteratorInputFactory inputNodes, FileFormat outputFormat,
			boolean writeMetadata, PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.pathsRelations = pathsRelations;
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

		// Extract relation member ids for each batch

		Path[] dirsData = pathsRelations.toArray(new Path[0]);

		MemberIdsExtractor memberIdsExtractor = new MemberIdsExtractor(
				dirsData, fileNamesRelations, fileNamesRelationNodeIds,
				fileNamesRelationWayIds, outputFormat);
		memberIdsExtractor.execute();

		// Extract ways for each batch

		List<ExtractionItem> wayExtractionItems = new ArrayList<>();
		for (Path path : pathsRelations) {
			wayExtractionItems.addAll(ExtractionUtil.createExtractionItems(
					path, fileNamesRelationWayIds, fileNamesWays));
		}

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
		for (Path path : pathsRelations) {
			nodeExtractionItems.addAll(ExtractionUtil.createExtractionItems(
					path, fileNamesNodeIds, fileNamesNodes));
		}

		Extractor nodeExtractor = new Extractor(EntityType.Node,
				nodeExtractionItems, outputFormat, pbfConfig, tboConfig,
				writeMetadata);
		OsmIteratorInput nodeInput = inputNodes.createIterator(writeMetadata);
		nodeExtractor.execute(nodeInput.getIterator());
		nodeInput.close();
	}

}

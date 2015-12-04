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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.CompositeOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.NullOsmEntityProvider;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;
import de.topobyte.osm4j.extra.util.RelationUtil;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public class SimpleRelationsDistributor extends RelationsDistributorBase
{

	public SimpleRelationsDistributor(Path pathTree, Path pathData,
			Path pathOutputEmpty, Path pathOutputNonTree,
			Path pathOutputBboxes, String fileNamesRelations,
			String fileNamesWays, String fileNamesNodes,
			String fileNamesTreeRelations, FileFormat inputFormat,
			OsmOutputConfig outputConfig)
	{
		super(pathTree, pathData, pathOutputEmpty, pathOutputNonTree,
				pathOutputBboxes, fileNamesRelations, fileNamesWays,
				fileNamesNodes, fileNamesTreeRelations, inputFormat,
				outputConfig);
	}

	public void execute() throws IOException
	{
		init();
		run();
		finish();
	}

	@Override
	protected void build(Path path) throws IOException
	{
		Path pathRelations = path.resolve(fileNamesRelations);
		Path pathWays = path.resolve(fileNamesWays);
		Path pathNodes = path.resolve(fileNamesNodes);

		InMemoryMapDataSet dataWays = read(pathWays, false, false);
		InMemoryMapDataSet dataNodes = read(pathNodes, false, false);

		OsmEntityProvider entityProvider = new CompositeOsmEntityProvider(
				dataNodes, dataWays, new NullOsmEntityProvider());

		InputStream input = StreamUtil.bufferedInputStream(pathRelations
				.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, outputConfig.isWriteMetadata());
		RelationIterator relationIterator = new RelationIterator(osmIterator);

		for (OsmRelation relation : relationIterator) {
			Set<OsmNode> nodes;
			try {
				nodes = RelationUtil.findNodes(relation, entityProvider);
			} catch (EntityNotFoundException e) {
				continue;
			}

			if (nodes.size() == 0) {
				nWrittenEmpty++;
				write(relation, outputEmpty);
				continue;
			}

			Envelope envelope = box(nodes);
			Geometry box = box(envelope);
			List<Node> leafs = tree.query(box);

			if (leafs.size() == 1) {
				nWrittenToTree++;
				write(relation, outputs.get(leafs.get(0)));
			} else {
				nRemaining++;
				write(relation, outputNonTree);
				outputBboxes.write(new IdBboxEntry(relation.getId(), envelope,
						nodes.size()));
			}
		}
	}

	private void write(OsmRelation relation, OsmStreamOutput output)
			throws IOException
	{
		output.getOsmOutput().write(relation);
	}

}

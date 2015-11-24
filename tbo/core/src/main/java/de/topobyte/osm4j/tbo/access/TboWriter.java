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

package de.topobyte.osm4j.tbo.access;

import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.batching.BatchBuilder;
import de.topobyte.osm4j.tbo.batching.ElementCountBatchBuilder;
import de.topobyte.osm4j.tbo.batching.MemberCountBatchBuilder;
import de.topobyte.osm4j.tbo.batching.WayNodeCountBatchBuilder;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.writerhelper.NodeBatch;
import de.topobyte.osm4j.tbo.writerhelper.RelationBatch;
import de.topobyte.osm4j.tbo.writerhelper.WayBatch;

public class TboWriter implements OsmOutputStream
{

	private BatchBuilder<OsmNode> batchBuilderNodes = new ElementCountBatchBuilder<>(
			Definitions.DEFAULT_BATCH_SIZE_NODES);
	private BatchBuilder<OsmWay> batchBuilderWays = new WayNodeCountBatchBuilder(
			Definitions.DEFAULT_BATCH_SIZE_WAY_NODES);
	private BatchBuilder<OsmRelation> batchBuilderRelations = new MemberCountBatchBuilder(
			Definitions.DEFAULT_BATCH_SIZE_RELATION_MEMBERS);

	private BlockWriter blockWriter;
	private BlockableWriter blockableWriter;

	private Compression compression = Compression.NONE;
	private boolean writeMetadata;

	private NodeBatch nodeBatch;
	private WayBatch wayBatch;
	private RelationBatch relationBatch;

	public TboWriter(OutputStream output, boolean writeMetadata)
	{
		this(new OutputStreamCompactWriter(output), writeMetadata);
	}

	public TboWriter(OutputStream output, boolean writeMetadata,
			boolean lowMemoryFootPrint)
	{
		this(new OutputStreamCompactWriter(output), writeMetadata,
				lowMemoryFootPrint);
	}

	public TboWriter(CompactWriter writer, boolean writeMetadata)
	{
		this(writer, writeMetadata, false);
	}

	public TboWriter(CompactWriter writer, boolean writeMetadata,
			boolean lowMemoryFootPrint)
	{
		this(new DefaultBlockWriter(writer), writeMetadata, lowMemoryFootPrint);
	}

	public TboWriter(BlockWriter blockWriter, boolean writeMetadata,
			boolean lowMemoryFootPrint)
	{
		blockableWriter = new BlockableWriter(blockWriter, lowMemoryFootPrint);

		this.blockWriter = blockWriter;
		this.writeMetadata = writeMetadata;

		nodeBatch = new NodeBatch(writeMetadata);
		wayBatch = new WayBatch(writeMetadata);
		relationBatch = new RelationBatch(writeMetadata);
	}

	public Compression getCompression()
	{
		return compression;
	}

	public void setCompression(Compression compression)
	{
		this.compression = compression;
	}

	public boolean isWriteMetadata()
	{
		return writeMetadata;
	}

	public void setWriteMetadata(boolean writeMetadata)
	{
		this.writeMetadata = writeMetadata;
	}

	public void setBatchSizeByElementCount(int batchSize)
	{
		batchBuilderNodes = new ElementCountBatchBuilder<>(batchSize);
		batchBuilderWays = new ElementCountBatchBuilder<>(batchSize);
		batchBuilderRelations = new ElementCountBatchBuilder<>(batchSize);
	}

	public void setBatchSizeByElementCount(int batchSizeNodes,
			int batchSizeWays, int batchSizeRelations)
	{
		batchBuilderNodes = new ElementCountBatchBuilder<>(batchSizeNodes);
		batchBuilderWays = new ElementCountBatchBuilder<>(batchSizeWays);
		batchBuilderRelations = new ElementCountBatchBuilder<>(
				batchSizeRelations);
	}

	public void setBatchSizeNodesByElementCount(int batchSize)
	{
		batchBuilderNodes = new ElementCountBatchBuilder<>(batchSize);
	}

	public void setBatchSizeWaysByElementCount(int batchSize)
	{
		batchBuilderWays = new ElementCountBatchBuilder<>(batchSize);
	}

	public void setBatchSizeRelationsByElementCount(int batchSize)
	{
		batchBuilderRelations = new ElementCountBatchBuilder<>(batchSize);
	}

	public void setBatchSizeWaysByNodes(int batchSize)
	{
		batchBuilderWays = new WayNodeCountBatchBuilder(batchSize);
	}

	public void setBatchSizeRelationsByMembers(int batchSize)
	{
		batchBuilderRelations = new MemberCountBatchBuilder(batchSize);
	}

	private enum Mode {
		HEADER,
		NODE,
		WAY,
		RELATION
	}

	private Mode mode = Mode.HEADER;

	private FileHeader header = null;

	/*
	 * external writing methods
	 */

	public void writeHeader(FileHeader header) throws IOException
	{
		this.header = header;
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		writeBounds(bounds);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		writeNode(node);
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		writeWay(way);
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		writeRelation(relation);
	}

	/*
	 * internal writing methods
	 */

	private void writeBounds(OsmBounds bounds) throws IOException
	{
		if (mode != Mode.HEADER) {
			throw new RuntimeException(
					"wrong entity order while processing bounds");
		}
		if (header == null) {
			header = WriterUtil.createHeader(writeMetadata, bounds);
		} else if (!header.hasBounds()) {
			// Don't overwrite bounds that have been set in the header manually
			header.setBounds(bounds);
		}
		finishHeader();
	}

	private void writeNode(OsmNode node) throws IOException
	{
		if (mode != Mode.NODE) {
			if (mode == Mode.HEADER) {
				finishHeader();
			} else {
				throw new RuntimeException(
						"wrong entity order while processing node");
			}
		}
		nodeBatch.put(node);
		if (checkBatch(node, batchBuilderNodes)) {
			writeNodeBatch();
		}
	}

	private void writeWay(OsmWay way) throws IOException
	{
		if (mode != Mode.WAY) {
			if (mode == Mode.HEADER) {
				finishHeader();
			} else if (mode == Mode.NODE) {
				finishNodes();
			} else {
				throw new RuntimeException(
						"wrong entity order while processing way");
			}
		}
		wayBatch.put(way);
		if (checkBatch(way, batchBuilderWays)) {
			writeWayBatch();
		}
	}

	private void writeRelation(OsmRelation relation) throws IOException
	{
		if (mode != Mode.RELATION) {
			if (mode == Mode.HEADER) {
				finishHeader();
			} else if (mode == Mode.NODE) {
				finishNodes();
				finishWays();
			} else if (mode == Mode.WAY) {
				finishWays();
			}
		}
		relationBatch.put(relation);
		if (checkBatch(relation, batchBuilderRelations)) {
			writeRelationBatch();
		}
	}

	private static <T extends OsmEntity> boolean checkBatch(T element,
			BatchBuilder<T> batchBuilder) throws IOException
	{
		batchBuilder.add(element);
		boolean full = batchBuilder.full();
		if (full) {
			batchBuilder.clear();
		}
		return full;
	}

	private void writeNodeBatch() throws IOException
	{
		blockableWriter.writeBlock(nodeBatch, Definitions.BLOCK_TYPE_NODES,
				nodeBatch.size(), compression);
		nodeBatch.clear();
	}

	private void writeWayBatch() throws IOException
	{
		blockableWriter.writeBlock(wayBatch, Definitions.BLOCK_TYPE_WAYS,
				wayBatch.size(), compression);
		wayBatch.clear();
	}

	private void writeRelationBatch() throws IOException
	{
		blockableWriter.writeBlock(relationBatch,
				Definitions.BLOCK_TYPE_RELATIONS, relationBatch.size(),
				compression);
		relationBatch.clear();
	}

	@Override
	public void complete() throws IOException
	{
		finishHeader();
		finishNodes();
		finishWays();
		finishRelations();
	}

	private void finishHeader() throws IOException
	{
		if (mode == Mode.HEADER) {
			if (header == null) {
				header = WriterUtil.createHeader(writeMetadata, null);
			}
			blockWriter.writeHeader(header);
			mode = Mode.NODE;
		}
	}

	private void finishNodes() throws IOException
	{
		if (mode == Mode.NODE) {
			if (nodeBatch.size() > 0) {
				writeNodeBatch();
			}
			mode = Mode.WAY;
		}
	}

	private void finishWays() throws IOException
	{
		if (mode == Mode.WAY) {
			if (wayBatch.size() > 0) {
				writeWayBatch();
			}
			mode = Mode.RELATION;
		}
	}

	private void finishRelations() throws IOException
	{
		if (mode == Mode.RELATION) {
			if (relationBatch.size() > 0) {
				writeRelationBatch();
			}
		}
	}

}

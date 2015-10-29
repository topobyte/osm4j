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

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.CompactWriter;
import de.topobyte.osm4j.tbo.io.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.writerhelper.NodeBag;
import de.topobyte.osm4j.tbo.writerhelper.RelationBag;
import de.topobyte.osm4j.tbo.writerhelper.WayBag;

public class TboWriter extends BlockWriter implements OsmOutputStream
{

	private int batchSizeNodes = Definitions.DEFAULT_BATCH_SIZE;
	private int batchSizeWays = Definitions.DEFAULT_BATCH_SIZE;
	private int batchSizeRelations = Definitions.DEFAULT_BATCH_SIZE;

	private boolean writeMetadata;

	private NodeBag nodeBag;
	private WayBag wayBag;
	private RelationBag relationBag;

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
		super(writer, lowMemoryFootPrint);
		this.writeMetadata = writeMetadata;

		initBags();
	}

	public void setBatchSize(int batchSize)
	{
		this.batchSizeNodes = batchSize;
		this.batchSizeWays = batchSize;
		this.batchSizeRelations = batchSize;
		initBags();
	}

	public void setBatchSize(int batchSizeNodes, int batchSizeWays,
			int batchSizeRelations)
	{
		this.batchSizeNodes = batchSizeNodes;
		this.batchSizeWays = batchSizeWays;
		this.batchSizeRelations = batchSizeRelations;
		initBags();
	}

	public int getBatchSizeNodes()
	{
		return batchSizeNodes;
	}

	public void setBatchSizeNodes(int batchSizeNodes)
	{
		this.batchSizeNodes = batchSizeNodes;
		initBags();
	}

	public int getBatchSizeWays()
	{
		return batchSizeWays;
	}

	public void setBatchSizeWays(int batchSizeWays)
	{
		this.batchSizeWays = batchSizeWays;
		initBags();
	}

	public int getBatchSizeRelations()
	{
		return batchSizeRelations;
	}

	public void setBatchSizeRelations(int batchSizeRelations)
	{
		this.batchSizeRelations = batchSizeRelations;
		initBags();
	}

	private void initBags()
	{
		nodeBag = new NodeBag(batchSizeNodes);
		wayBag = new WayBag(batchSizeWays);
		relationBag = new RelationBag(batchSizeRelations);
	}

	private enum Mode {
		HEADER,
		NODE,
		WAY,
		RELATION
	}

	private Mode mode = Mode.HEADER;
	private int counterNodes = 0;
	private int counterWays = 0;
	private int counterRelations = 0;

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
		nodeBag.put(node);
		counterNodes++;
		if (counterNodes == batchSizeNodes) {
			writeBlock(nodeBag, Definitions.BLOCK_TYPE_NODES, counterNodes);
			nodeBag.clear();
			counterNodes = 0;
		}

	}

	private void writeWay(OsmWay way) throws IOException
	{
		if (mode != Mode.WAY) {
			if (mode == Mode.NODE) {
				finishNodes();
			} else {
				throw new RuntimeException(
						"wrong entity order while processing way");
			}
		}
		wayBag.put(way);
		counterWays++;
		if (counterWays == batchSizeWays) {
			writeBlock(wayBag, Definitions.BLOCK_TYPE_WAYS, counterWays);
			wayBag.clear();
			counterWays = 0;
		}
	}

	private void writeRelation(OsmRelation relation) throws IOException
	{
		if (mode != Mode.RELATION) {
			if (mode == Mode.NODE) {
				finishNodes();
				finishWays();
			} else if (mode == Mode.WAY) {
				finishWays();
			}
		}
		relationBag.put(relation);
		counterRelations++;
		if (counterRelations == batchSizeRelations) {
			writeBlock(relationBag, Definitions.BLOCK_TYPE_RELATIONS,
					counterRelations);
			relationBag.clear();
			counterRelations = 0;
		}
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
			header.write(writer);
			mode = Mode.NODE;
		}
	}

	private void finishNodes() throws IOException
	{
		if (mode == Mode.NODE) {
			if (counterNodes > 0) {
				writeBlock(nodeBag, Definitions.BLOCK_TYPE_NODES, counterNodes);
				nodeBag.clear();
				counterNodes = 0;
			}
			mode = Mode.WAY;
		}
	}

	private void finishWays() throws IOException
	{
		if (mode == Mode.WAY) {
			if (counterWays > 0) {
				writeBlock(wayBag, Definitions.BLOCK_TYPE_WAYS, counterWays);
				wayBag.clear();
				counterWays = 0;
			}
			mode = Mode.RELATION;
		}
	}

	private void finishRelations() throws IOException
	{
		if (mode == Mode.RELATION) {
			if (counterRelations > 0) {
				writeBlock(relationBag, Definitions.BLOCK_TYPE_RELATIONS,
						counterRelations);
				relationBag.clear();
				counterRelations = 0;
			}
		}
	}

}

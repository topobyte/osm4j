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
import de.topobyte.osm4j.tbo.data.Metadata;
import de.topobyte.osm4j.tbo.io.CompactWriter;
import de.topobyte.osm4j.tbo.io.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.writerhelper.NodeBag;
import de.topobyte.osm4j.tbo.writerhelper.RelationBag;
import de.topobyte.osm4j.tbo.writerhelper.WayBag;

public class TboWriter extends BlockWriter implements OsmOutputStream
{

	private final int batchSize;

	private NodeBag nodeBag;
	private WayBag wayBag;
	private RelationBag relationBag;

	public TboWriter(OutputStream output)
	{
		this(new OutputStreamCompactWriter(output),
				Definitions.DEFAULT_BATCH_SIZE);
	}

	public TboWriter(OutputStream output, boolean lowMemoryFootPrint)
	{
		this(new OutputStreamCompactWriter(output),
				Definitions.DEFAULT_BATCH_SIZE, lowMemoryFootPrint);
	}

	public TboWriter(CompactWriter writer, int batchSize)
	{
		this(writer, batchSize, false);
	}

	public TboWriter(CompactWriter writer, int batchSize,
			boolean lowMemoryFootPrint)
	{
		super(writer, lowMemoryFootPrint);
		this.batchSize = batchSize;

		nodeBag = new NodeBag(batchSize);
		wayBag = new WayBag(batchSize);
		relationBag = new RelationBag(batchSize);
	}

	private enum Mode {
		NODE,
		WAY,
		RELATION
	}

	private Mode mode = Mode.NODE;
	private int counterNodes = 0;
	private int counterWays = 0;
	private int counterRelations = 0;

	/*
	 * external writing methods
	 */

	public void writeMetadata(Metadata metadata) throws IOException
	{
		writeBlock(metadata, Definitions.BLOCK_TYPE_METADATA, 0);
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		// not supported at them moment
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

	private void writeNode(OsmNode node) throws IOException
	{
		if (mode != Mode.NODE) {
			throw new RuntimeException(
					"wrong entity order while processing node");
		}
		nodeBag.put(node);
		counterNodes++;
		if (counterNodes == batchSize) {
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
				mode = Mode.WAY;
			} else {
				throw new RuntimeException(
						"wrong entity order while processing way");
			}
		}
		wayBag.put(way);
		counterWays++;
		if (counterWays == batchSize) {
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
				mode = Mode.RELATION;
			} else if (mode == Mode.WAY) {
				finishWays();
				mode = Mode.RELATION;
			}
		}
		relationBag.put(relation);
		counterRelations++;
		if (counterRelations == batchSize) {
			writeBlock(relationBag, Definitions.BLOCK_TYPE_RELATIONS,
					counterRelations);
			relationBag.clear();
			counterRelations = 0;
		}
	}

	@Override
	public void complete() throws IOException
	{
		finishNodes();
		finishWays();
		finishRelations();
	}

	private void finishNodes() throws IOException
	{
		if (counterNodes > 0) {
			writeBlock(nodeBag, Definitions.BLOCK_TYPE_NODES, counterNodes);
			nodeBag.clear();
			counterNodes = 0;
		}
	}

	private void finishWays() throws IOException
	{
		if (counterWays > 0) {
			writeBlock(wayBag, Definitions.BLOCK_TYPE_WAYS, counterWays);
			wayBag.clear();
			counterWays = 0;
		}
	}

	private void finishRelations() throws IOException
	{
		if (counterRelations > 0) {
			writeBlock(relationBag, Definitions.BLOCK_TYPE_RELATIONS,
					counterRelations);
			relationBag.clear();
			counterRelations = 0;
		}
	}

}

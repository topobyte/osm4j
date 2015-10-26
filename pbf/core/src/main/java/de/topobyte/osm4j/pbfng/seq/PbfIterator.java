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

package de.topobyte.osm4j.pbfng.seq;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBBox;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbfng.Constants;
import de.topobyte.osm4j.pbfng.util.BlobHeader;
import de.topobyte.osm4j.pbfng.util.BlockData;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public class PbfIterator implements OsmIterator
{

	private DataInputStream input;
	private boolean fetchMetadata;

	private OsmBounds bounds = null;
	private boolean beyondBounds = false;

	private List<OsmNode> nodes = new LinkedList<>();
	private List<OsmWay> ways = new LinkedList<>();
	private List<OsmRelation> relations = new LinkedList<>();

	private int available = 0;
	private boolean finished = false;

	public PbfIterator(InputStream input, boolean fetchMetadata)
	{
		this.input = new DataInputStream(input);
		this.fetchMetadata = fetchMetadata;
	}

	@Override
	public boolean hasNext()
	{
		if (available > 0) {
			return true;
		}
		while (!finished && available == 0) {
			try {
				tryAdvanceBlock();
			} catch (IOException e) {
				throw new RuntimeException("error while reading block", e);
			}
		}
		return available > 0;
	}

	@Override
	public EntityContainer next()
	{
		while (available == 0) {
			if (finished) {
				throw new NoSuchElementException();
			}
			try {
				tryAdvanceBlock();
			} catch (IOException e) {
				throw new RuntimeException("error while reading block", e);
			}
		}
		available--;
		if (nodes.size() > 0) {
			OsmNode node = nodes.remove(0);
			return new EntityContainer(EntityType.Node, node);
		} else if (ways.size() > 0) {
			OsmWay way = ways.remove(0);
			return new EntityContainer(EntityType.Way, way);
		} else {
			OsmRelation relation = relations.remove(0);
			return new EntityContainer(EntityType.Relation, relation);
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("read only");
	}

	private void tryAdvanceBlock() throws IOException
	{
		try {
			advanceBlock();
		} catch (EOFException eof) {
			finished = true;
			beyondBounds = true;
		}
	}

	private void advanceBlock() throws IOException
	{
		BlobHeader header = PbfUtil.parseHeader(input);
		Fileformat.Blob blob = PbfUtil
				.parseBlock(input, header.getDataLength());

		BlockData blockData = PbfUtil.getBlockData(blob);

		String type = header.getType();
		if (type.equals(Constants.BLOCK_TYPE_DATA)) {
			beyondBounds = true;

			Osmformat.PrimitiveBlock block = Osmformat.PrimitiveBlock
					.parseFrom(blockData.getBlobData());

			PrimParser primParser = new PrimParser(block, fetchMetadata);

			for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
				if (group.getNodesCount() > 0) {
					pushNodes(primParser, group.getNodesList());
				}
				if (group.hasDense()) {
					pushNodes(primParser, group.getDense());
				}
				if (group.getWaysCount() > 0) {
					pushWays(primParser, group.getWaysList());
				}
				if (group.getRelationsCount() > 0) {
					pushRelations(primParser, group.getRelationsList());
				}
			}
		} else if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
			Osmformat.HeaderBlock block = Osmformat.HeaderBlock
					.parseFrom(blockData.getBlobData());
			HeaderBBox bbox = block.getBbox();
			if (bbox != null && !beyondBounds) {
				this.bounds = PbfUtil.bounds(bbox);
			}
			beyondBounds = true;
		}
	}

	private void pushNodes(PrimParser primParser, List<Osmformat.Node> nodes)
	{
		available += nodes.size();
		for (Osmformat.Node node : nodes) {
			this.nodes.add(primParser.convert(node));
		}
	}

	private void pushNodes(PrimParser primParser, DenseNodes dense)
	{
		List<OsmNode> nodes = primParser.convert(dense);
		available += nodes.size();
		this.nodes.addAll(nodes);
	}

	private void pushWays(PrimParser primParser, List<Way> ways)
	{
		available += ways.size();
		for (Osmformat.Way way : ways) {
			this.ways.add(primParser.convert(way));
		}
	}

	private void pushRelations(PrimParser primParser, List<Relation> relations)
	{
		available += relations.size();
		for (Osmformat.Relation relation : relations) {
			this.relations.add(primParser.convert(relation));
		}
	}

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasBounds()
	{
		ensureBeyondBounds();
		return bounds != null;
	}

	@Override
	public OsmBounds getBounds()
	{
		ensureBeyondBounds();
		return bounds;
	}

	private void ensureBeyondBounds()
	{
		while (!beyondBounds) {
			try {
				tryAdvanceBlock();
			} catch (IOException e) {
				throw new RuntimeException("error while reading block", e);
			}
		}
	}

}

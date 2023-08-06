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

package de.topobyte.osm4j.pbf.seq;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbf.Compression;
import de.topobyte.osm4j.pbf.Constants;
import de.topobyte.osm4j.pbf.protobuf.Fileformat;
import de.topobyte.osm4j.pbf.protobuf.Osmformat;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.PrimitiveBlock;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.PrimitiveBlock.Builder;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.PrimitiveGroup;
import de.topobyte.osm4j.pbf.util.BlobHeader;
import de.topobyte.osm4j.pbf.util.BlockData;
import de.topobyte.osm4j.pbf.util.PbfMeta;
import de.topobyte.osm4j.pbf.util.PbfUtil;
import de.topobyte.osm4j.pbf.util.copy.EntityGroups;

public class PbfEntitySplit
{

	final static Logger logger = LoggerFactory.getLogger(PbfEntitySplit.class);

	private final OutputStream outNodes, outWays, outRelations;

	private boolean copyNodes;
	private boolean copyWays;
	private boolean copyRelations;

	private DataInputStream input;
	private BlockWriter blockWriterNodes = null;
	private BlockWriter blockWriterWays = null;
	private BlockWriter blockWriterRelations = null;

	public PbfEntitySplit(InputStream in, OutputStream outNodes,
			OutputStream outWays, OutputStream outRelations)
	{
		this.outNodes = outNodes;
		this.outWays = outWays;
		this.outRelations = outRelations;

		input = new DataInputStream(in);

		copyNodes = outNodes != null;
		copyWays = outWays != null;
		copyRelations = outRelations != null;

		if (copyNodes) {
			blockWriterNodes = new BlockWriter(outNodes);
		}
		if (copyWays) {
			blockWriterWays = new BlockWriter(outWays);
		}
		if (copyRelations) {
			blockWriterRelations = new BlockWriter(outRelations);
		}
	}

	public void execute() throws IOException
	{
		while (true) {
			try {
				BlobHeader header = PbfUtil.parseHeader(input);

				Fileformat.Blob blob = PbfUtil.parseBlock(input,
						header.getDataLength());

				String type = header.getType();

				// TODO: stop iterating if not all entity type are requested
				// (for example only nodes) and we are beyond the last block of
				// requested types
				if (type.equals(Constants.BLOCK_TYPE_DATA)) {
					data(blob);
				} else if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
					if (copyNodes) {
						blockWriterNodes.write(header.getType(), null, blob);
					}
					if (copyWays) {
						blockWriterWays.write(header.getType(), null, blob);
					}
					if (copyRelations) {
						blockWriterRelations.write(header.getType(), null,
								blob);
					}
				}

			} catch (EOFException eof) {
				break;
			}
		}

		if (copyNodes) {
			outNodes.close();
		}
		if (copyWays) {
			outWays.close();
		}
		if (copyRelations) {
			outRelations.close();
		}

	}

	private void data(Fileformat.Blob blob) throws IOException
	{
		BlockData blockData = PbfUtil.getBlockData(blob);
		Osmformat.PrimitiveBlock primBlock = Osmformat.PrimitiveBlock
				.parseFrom(blockData.getBlobData());

		if (!PbfMeta.hasMixedContent(primBlock)) {
			// If the block does not contain multiple entity types, we can copy
			// the blob without have to recreate the message.
			EntityType type = PbfMeta.getContentTypes(primBlock).iterator()
					.next();
			if (type == EntityType.Node && copyNodes) {
				blockWriterNodes.write(Constants.BLOCK_TYPE_DATA, null, blob);
			} else if (type == EntityType.Way && copyWays) {
				blockWriterWays.write(Constants.BLOCK_TYPE_DATA, null, blob);
			} else if (type == EntityType.Relation && copyRelations) {
				blockWriterRelations.write(Constants.BLOCK_TYPE_DATA, null,
						blob);
			}
		} else {
			// Multiple entity types in the block. Extract types and write to
			// appropriate output.
			EntityGroups groups = EntityGroups.splitEntities(primBlock);

			Compression compression = blockData.getCompression();

			if (copyNodes && groups.getNodeGroups().size() > 0) {
				copy(blockWriterNodes, groups.getNodeGroups(), primBlock,
						compression);
			}

			if (copyWays && groups.getWayGroups().size() > 0) {
				copy(blockWriterWays, groups.getWayGroups(), primBlock,
						compression);
			}

			if (copyRelations && groups.getRelationGroups().size() > 0) {
				copy(blockWriterRelations, groups.getRelationGroups(),
						primBlock, compression);
			}
		}

	}

	private void copy(BlockWriter blockWriter, List<PrimitiveGroup> gs,
			Osmformat.PrimitiveBlock primBlock, Compression compression)
			throws IOException
	{
		Osmformat.PrimitiveBlock.Builder builder = Osmformat.PrimitiveBlock
				.newBuilder();
		for (Osmformat.PrimitiveGroup group : gs) {
			builder.addPrimitivegroup(group);
		}
		copyExtraData(builder, primBlock);
		Osmformat.PrimitiveBlock block = builder.build();
		blockWriter.write(Constants.BLOCK_TYPE_DATA, null, compression,
				block.toByteString());
	}

	private void copyExtraData(Builder builder, PrimitiveBlock primBlock)
	{
		builder.setGranularity(primBlock.getGranularity());
		builder.setDateGranularity(primBlock.getDateGranularity());
		builder.setStringtable(primBlock.getStringtable());
	}

}

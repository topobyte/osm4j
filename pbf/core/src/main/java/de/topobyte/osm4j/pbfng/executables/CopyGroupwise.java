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

package de.topobyte.osm4j.pbfng.executables;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.google.protobuf.ByteString;

import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import de.topobyte.osm4j.pbfng.Constants;
import de.topobyte.osm4j.pbfng.seq.BlockWriter;
import de.topobyte.osm4j.pbfng.util.BlobHeader;
import de.topobyte.osm4j.pbfng.util.BlockData;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public class CopyGroupwise
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2) {
			System.out.println("usage: " + CopyGroupwise.class.getSimpleName()
					+ " <input> <output>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		OutputStream output = new FileOutputStream(args[1]);

		DataInputStream data = new DataInputStream(input);

		BlockWriter blockWriter = new BlockWriter(output);

		while (true) {
			try {
				BlobHeader header = PbfUtil.parseHeader(data);

				Fileformat.Blob blob = PbfUtil.parseBlock(data,
						header.getDataLength());

				String type = header.getType();

				if (type.equals(Constants.BLOCK_TYPE_DATA)) {
					BlockData blockData = PbfUtil.getBlockData(blob);
					Osmformat.PrimitiveBlock primBlock = Osmformat.PrimitiveBlock
							.parseFrom(blockData.getBlobData());

					Osmformat.PrimitiveBlock.Builder builder = Osmformat.PrimitiveBlock
							.newBuilder();

					for (int i = 0; i < primBlock.getPrimitivegroupCount(); i++) {
						Osmformat.PrimitiveGroup.Builder groupBuilder = Osmformat.PrimitiveGroup
								.newBuilder();
						Osmformat.PrimitiveGroup group = primBlock
								.getPrimitivegroup(i);
						List<Osmformat.Node> nodes = group.getNodesList();
						Osmformat.DenseNodes dense = group.getDense();
						List<Osmformat.Way> ways = group.getWaysList();
						List<Osmformat.Relation> relations = group
								.getRelationsList();

						groupBuilder.addAllNodes(nodes);
						if (group.hasDense()) {
							groupBuilder.setDense(dense);
						}
						groupBuilder.addAllWays(ways);
						groupBuilder.addAllRelations(relations);
						builder.addPrimitivegroup(groupBuilder.build());
					}

					builder.setGranularity(primBlock.getGranularity());
					builder.setDateGranularity(primBlock.getDateGranularity());
					builder.setStringtable(primBlock.getStringtable());

					Osmformat.PrimitiveBlock block = builder.build();
					ByteString message = block.toByteString();

					blockWriter.write(header.getType(), null,
							blockData.getCompression(), message);
				} else if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
					blockWriter.write(header.getType(), null, blob);
				}

			} catch (EOFException eof) {
				break;
			}
		}

		output.close();
	}
}

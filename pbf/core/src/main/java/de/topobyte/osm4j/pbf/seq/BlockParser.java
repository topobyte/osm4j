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

import java.io.IOException;

import de.topobyte.osm4j.pbf.Constants;
import de.topobyte.osm4j.pbf.protobuf.Fileformat;
import de.topobyte.osm4j.pbf.protobuf.Osmformat;
import de.topobyte.osm4j.pbf.util.BlobHeader;
import de.topobyte.osm4j.pbf.util.BlockData;
import de.topobyte.osm4j.pbf.util.PbfUtil;

public abstract class BlockParser extends BlobParser
{

	@Override
	protected void parse(BlobHeader header, Fileformat.Blob blob)
			throws IOException
	{
		BlockData blockData = PbfUtil.getBlockData(blob);

		String type = header.getType();
		if (type.equals(Constants.BLOCK_TYPE_DATA)) {
			Osmformat.PrimitiveBlock primBlock = Osmformat.PrimitiveBlock
					.parseFrom(blockData.getBlobData());
			parse(primBlock);
		} else if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
			Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock
					.parseFrom(blockData.getBlobData());
			parse(headerBlock);
		} else {
			throw new IOException("invalid PBF block");
		}
	}

	protected abstract void parse(Osmformat.HeaderBlock block)
			throws IOException;

	protected abstract void parse(Osmformat.PrimitiveBlock block)
			throws IOException;

}

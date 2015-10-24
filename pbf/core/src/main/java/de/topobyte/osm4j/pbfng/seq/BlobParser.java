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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import crosby.binary.Fileformat;
import de.topobyte.osm4j.pbfng.util.BlockHeader;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public abstract class BlobParser
{

	public void parse(InputStream input) throws IOException
	{
		DataInputStream data = new DataInputStream(input);
		while (true) {
			try {
				parseBlob(data);
			} catch (EOFException eof) {
				break;
			}
		}
	}

	private void parseBlob(DataInput data) throws IOException
	{
		BlockHeader header = PbfUtil.parseHeader(data);
		Fileformat.Blob blob = PbfUtil.parseBlock(data, header.getDataLength());

		parse(header, blob);
	}

	protected abstract void parse(BlockHeader header, Fileformat.Blob blob)
			throws IOException;

}

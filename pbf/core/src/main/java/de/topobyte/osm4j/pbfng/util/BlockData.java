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

package de.topobyte.osm4j.pbfng.util;

import com.google.protobuf.ByteString;

import de.topobyte.osm4j.pbfng.Compression;

public class BlockData
{

	private ByteString blobData;
	private Compression compression;

	public BlockData(ByteString blobData, Compression compression)
	{
		super();
		this.blobData = blobData;
		this.compression = compression;
	}

	public ByteString getBlobData()
	{
		return blobData;
	}

	public void setBlobData(ByteString blobData)
	{
		this.blobData = blobData;
	}

	public Compression getCompression()
	{
		return compression;
	}

	public void setCompression(Compression compression)
	{
		this.compression = compression;
	}

}

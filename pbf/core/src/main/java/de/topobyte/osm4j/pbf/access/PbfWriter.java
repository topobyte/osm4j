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

package de.topobyte.osm4j.pbf.access;

import java.io.IOException;
import java.io.OutputStream;

import crosby.binary.file.BlockOutputStream;
import crosby.binary.file.CompressFlags;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.PbfSerializer;

public class PbfWriter implements OsmOutputStream
{

	private PbfSerializer serializer;

	public PbfWriter(OutputStream output, boolean outputMetadata)
	{
		this(output, outputMetadata, true, true);
	}

	public PbfWriter(OutputStream output, boolean outputMetadata,
			boolean useCompression, boolean useDenseNodes)
	{
		BlockOutputStream blockOutputStream = new BlockOutputStream(output);
		CompressFlags flags = useCompression ? CompressFlags.DEFLATE
				: CompressFlags.NONE;
		blockOutputStream.setCompress(flags);
		serializer = new PbfSerializer(blockOutputStream, outputMetadata);
	}

	@Override
	public void complete() throws IOException
	{
		serializer.complete();
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		serializer.write(bounds);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		serializer.write(node);
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		serializer.write(way);
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		serializer.write(relation);
	}

}

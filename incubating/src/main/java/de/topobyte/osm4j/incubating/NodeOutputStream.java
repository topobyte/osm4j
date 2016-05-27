// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.incubating;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class NodeOutputStream implements OsmOutputStream
{

	private OutputStream output;
	private CompactWriter writer;

	private boolean writeTags;
	private boolean writeMetadata;

	private long idOffset = 0;
	private long latOffset = 0;
	private long lonOffset = 0;

	private long versionOffset = 0;
	private long changesetOffset = 0;
	private long timestampOffset = 0;
	private long uidOffset = 0;

	public NodeOutputStream(OutputStream output, boolean writeTags,
			boolean writeMetadata)
	{
		this.output = output;
		this.writeTags = writeTags;
		this.writeMetadata = writeMetadata;
		writer = new OutputStreamCompactWriter(output);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		long id = node.getId();

		writer.writeVariableLengthSignedInteger(id - idOffset);
		idOffset = id;

		double lat = node.getLatitude();
		double lon = node.getLongitude();
		long mlat = toLong(lat);
		long mlon = toLong(lon);

		writer.writeVariableLengthSignedInteger(mlat - latOffset);
		writer.writeVariableLengthSignedInteger(mlon - lonOffset);

		latOffset = mlat;
		lonOffset = mlon;

		writeTags(node);
		writeMetadata(node);
	}

	private void writeTags(OsmEntity entity) throws IOException
	{
		if (!writeTags) {
			writer.writeVariableLengthUnsignedInteger(0);
		} else {
			writer.writeVariableLengthUnsignedInteger(entity.getNumberOfTags());
			for (int i = 0; i < entity.getNumberOfTags(); i++) {
				OsmTag tag = entity.getTag(i);
				write(tag.getKey());
				write(tag.getValue());
			}
		}
	}

	private void writeMetadata(OsmEntity entity) throws IOException
	{
		OsmMetadata metadata = entity.getMetadata();
		if (!writeMetadata || metadata == null) {
			writer.writeByte(0);
		} else {
			writer.writeByte(1);
			int version = metadata.getVersion();
			long changeset = metadata.getChangeset();
			long timestamp = metadata.getTimestamp();
			long uid = metadata.getUid();
			writer.writeVariableLengthSignedInteger(version - versionOffset);
			writer.writeVariableLengthSignedInteger(changeset - changesetOffset);
			writer.writeVariableLengthSignedInteger(timestamp - timestampOffset);
			writer.writeVariableLengthSignedInteger(uid - uidOffset);
			// writer.writeLong(version);
			// writer.writeLong(changeset);
			// writer.writeLong(timestamp);
			// writer.writeLong(uid);
			// writer.writeString(metadata.getUser());
			write(metadata.getUser());
			versionOffset = version;
			changesetOffset = changeset;
			timestampOffset = timestamp;
			uidOffset = uid;
		}
	}

	private void write(String string) throws IOException
	{
		byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
		writer.writeVariableLengthSignedInteger(bytes.length);
		writer.write(bytes);
	}

	public void close() throws IOException
	{
		output.close();
	}

	private long toLong(double degrees)
	{
		return (long) (degrees / .0000001);
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		// ignore
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		// ignore
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		// ignore
	}

	@Override
	public void complete() throws IOException
	{
		close();
	}

}

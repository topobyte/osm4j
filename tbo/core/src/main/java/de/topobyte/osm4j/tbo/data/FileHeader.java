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

package de.topobyte.osm4j.tbo.data;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.tbo.io.CompactWriter;
import de.topobyte.osm4j.tbo.writerhelper.Blockable;

public class FileHeader implements Blockable
{

	public static final byte[] MAGIC = "tbo!".getBytes();

	public static final int FLAG_HAS_METADATA = 0x1;
	public static final int FLAG_HAS_BOUNDS = 0x2;

	private int version;

	private Map<String, String> tags = new TreeMap<>();

	private boolean hasMetadata;
	private OsmBounds bounds;

	public FileHeader(int version, Map<String, String> tags,
			boolean hasMetadata, OsmBounds bounds)
	{
		this.version = version;
		this.tags = tags;
		this.hasMetadata = hasMetadata;
		this.bounds = bounds;
	}

	public int getVersion()
	{
		return version;
	}

	public Map<String, String> getTags()
	{
		return tags;
	}

	public boolean hasTag(String key)
	{
		return tags.containsKey(key);
	}

	public String get(String key)
	{
		return tags.get(key);
	}

	public boolean hasMetadata()
	{
		return hasMetadata;
	}

	public boolean hasBounds()
	{
		return bounds != null;
	}

	public OsmBounds getBounds()
	{
		return bounds;
	}

	public void setBounds(OsmBounds bounds)
	{
		this.bounds = bounds;
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		writer.write(MAGIC);
		writer.writeVariableLengthUnsignedInteger(version);
		writer.writeVariableLengthUnsignedInteger(tags.size());
		for (Entry<String, String> entry : tags.entrySet()) {
			writer.writeString(entry.getKey());
			writer.writeString(entry.getValue());
		}
		int flags = 0;
		if (hasMetadata) {
			flags |= FLAG_HAS_METADATA;
		}
		if (hasBounds()) {
			flags |= FLAG_HAS_BOUNDS;
		}
		writer.writeByte(flags);
		if (hasBounds()) {
			writer.writeLong(Double.doubleToLongBits(bounds.getLeft()));
			writer.writeLong(Double.doubleToLongBits(bounds.getRight()));
			writer.writeLong(Double.doubleToLongBits(bounds.getBottom()));
			writer.writeLong(Double.doubleToLongBits(bounds.getTop()));
		}
	}

}

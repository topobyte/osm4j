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

import de.topobyte.osm4j.tbo.io.CompactWriter;
import de.topobyte.osm4j.tbo.writerhelper.Blockable;

public class Metadata implements Blockable
{

	private String version = null;

	private Map<String, String> tags = new TreeMap<>();

	public Metadata(String version, Map<String, String> tags)
	{
		this.version = version;
		this.tags = tags;
	}

	public String getVersion()
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

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		writer.writeString(version);
		writer.writeVariableLengthSignedInteger(tags.size());
		for (Entry<String, String> entry : tags.entrySet()) {
			writer.writeString(entry.getKey());
			writer.writeString(entry.getValue());
		}
	}

}

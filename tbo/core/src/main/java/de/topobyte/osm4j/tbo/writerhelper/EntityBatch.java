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

package de.topobyte.osm4j.tbo.writerhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.tbo.data.StringPool;
import de.topobyte.osm4j.tbo.data.StringPoolBuilder;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public abstract class EntityBatch<T extends OsmEntity> implements Blockable
{

	private boolean writeMetadata;

	protected List<T> elements;

	protected StringPool stringPoolTags;

	public EntityBatch(boolean writeMetadata)
	{
		this.writeMetadata = writeMetadata;
		elements = new ArrayList<>();
	}

	public void put(T node)
	{
		elements.add(node);
	}

	public void clear()
	{
		elements.clear();
	}

	public int size()
	{
		return elements.size();
	}

	public void writeTagStringPool(CompactWriter writer) throws IOException
	{
		StringPoolBuilder poolBuilder = new StringPoolBuilder();
		for (OsmEntity object : elements) {
			// add tags
			int nTags = object.getNumberOfTags();
			for (int i = 0; i < nTags; i++) {
				OsmTag tag = object.getTag(i);
				String key = tag.getKey();
				String value = tag.getValue();
				poolBuilder.add(key);
				poolBuilder.add(value);
			}
		}
		stringPoolTags = poolBuilder.buildStringPool();

		writePool(writer, stringPoolTags);
	}

	protected void writePool(CompactWriter writer, StringPool stringPool)
			throws IOException
	{
		int size = stringPool.size();
		writer.writeVariableLengthUnsignedInteger(size);
		for (int i = 0; i < size; i++) {
			String string = stringPool.getString(i);
			writer.writeString(string);
		}
	}

	protected void writeTags(CompactWriter writer, OsmEntity entity)
			throws IOException
	{
		int nTags = entity.getNumberOfTags();
		writer.writeVariableLengthUnsignedInteger(nTags);
		for (int i = 0; i < nTags; i++) {
			OsmTag tag = entity.getTag(i);
			String key = tag.getKey();
			String value = tag.getValue();
			int k = stringPoolTags.getId(key);
			int v = stringPoolTags.getId(value);
			writer.writeVariableLengthUnsignedInteger(k);
			writer.writeVariableLengthUnsignedInteger(v);
		}
	}

}

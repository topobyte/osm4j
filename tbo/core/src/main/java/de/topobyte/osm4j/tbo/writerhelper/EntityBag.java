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
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.tbo.data.StringPool;
import de.topobyte.osm4j.tbo.data.StringPoolBuilder;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public abstract class EntityBag<T extends OsmEntity> implements Blockable
{

	protected List<T> elements;

	protected StringPool stringPool;

	public EntityBag()
	{
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

	public void writeStringPool(CompactWriter writer) throws IOException
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
		stringPool = poolBuilder.buildStringPool();

		int size = stringPool.size();
		writer.writeVariableLengthUnsignedInteger(size);
		for (int i = 0; i < size; i++) {
			String string = stringPool.getString(i);
			writer.writeString(string);
		}
	}

	public void writeStringPool(CompactWriter writer, List<OsmRelation> objects)
			throws IOException
	{
		StringPoolBuilder poolBuilder = new StringPoolBuilder();
		for (OsmRelation object : objects) {
			// add roles
			int nMembers = object.getNumberOfMembers();
			for (int i = 0; i < nMembers; i++) {
				OsmRelationMember member = object.getMember(i);
				poolBuilder.add(member.getRole());
			}
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
		stringPool = poolBuilder.buildStringPool();

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
			int k = stringPool.getId(key);
			int v = stringPool.getId(value);
			writer.writeVariableLengthUnsignedInteger(k);
			writer.writeVariableLengthUnsignedInteger(v);
		}
	}

}

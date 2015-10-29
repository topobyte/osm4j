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
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.StringPool;
import de.topobyte.osm4j.tbo.data.StringPoolBuilder;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public abstract class EntityBatch<T extends OsmEntity> implements Blockable
{

	private boolean writeMetadata;

	protected List<T> elements;

	protected StringPool stringPoolTags;
	protected StringPool stringPoolUsernames;

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

	public void writeUsernameStringPool(CompactWriter writer)
			throws IOException
	{
		StringPoolBuilder poolBuilder = new StringPoolBuilder();
		for (OsmEntity object : elements) {
			// add user names
			OsmMetadata metadata = object.getMetadata();
			if (metadata == null) {
				continue;
			}
			poolBuilder.add(metadata.getUser());
		}
		stringPoolUsernames = poolBuilder.buildStringPool();

		writePool(writer, stringPoolUsernames);
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

	protected void writeMetadata(CompactWriter writer) throws IOException
	{
		if (!writeMetadata) {
			return;
		}

		// Determine the situation among all elements
		boolean none = true;
		boolean all = true;
		for (OsmEntity element : elements) {
			if (element.getMetadata() == null) {
				all = false;
			} else {
				none = false;
			}
			if (!all && !none) {
				// mixed
				break;
			}
		}

		int situation;
		if (all) {
			situation = Definitions.METADATA_ALL;
		} else if (none) {
			situation = Definitions.METADATA_NONE;
		} else {
			situation = Definitions.METADATA_MIXED;
		}

		writer.writeByte(situation);

		if (none) {
			return;
		}

		writeUsernameStringPool(writer);

		if (!all) {
			writeFlags(writer);
		}

		writeVersions(writer);
		writeTimestamps(writer);
		writeChangesets(writer);
		writerUserIds(writer);
		writeUsernames(writer);
	}

	private void writeFlags(CompactWriter writer) throws IOException
	{
		// write flags for each element
		for (OsmEntity element : elements) {
			if (element.getMetadata() == null) {
				writer.writeByte(Definitions.METADATA_NO);
			} else {
				writer.writeByte(Definitions.METADATA_YES);
			}
		}
	}

	private void writeVersions(CompactWriter writer) throws IOException
	{
		int offset = 0;
		for (OsmEntity element : elements) {
			OsmMetadata metadata = element.getMetadata();
			if (metadata != null) {
				int version = metadata.getVersion();
				writer.writeVariableLengthSignedInteger(version - offset);
				offset = version;
			}
		}
	}

	private void writeTimestamps(CompactWriter writer) throws IOException
	{
		long offset = 0;
		for (OsmEntity element : elements) {
			OsmMetadata metadata = element.getMetadata();
			if (metadata != null) {
				long timestamp = metadata.getTimestamp();
				writer.writeVariableLengthSignedInteger(timestamp - offset);
				offset = timestamp;
			}
		}
	}

	private void writeChangesets(CompactWriter writer) throws IOException
	{
		long offset = 0;
		for (OsmEntity element : elements) {
			OsmMetadata metadata = element.getMetadata();
			if (metadata != null) {
				long changeset = metadata.getChangeset();
				writer.writeVariableLengthSignedInteger(changeset - offset);
				offset = changeset;
			}
		}
	}

	private void writerUserIds(CompactWriter writer) throws IOException
	{
		long offset = 0;
		for (OsmEntity element : elements) {
			OsmMetadata metadata = element.getMetadata();
			if (metadata != null) {
				long userId = metadata.getUid();
				writer.writeVariableLengthSignedInteger(userId - offset);
				offset = userId;
			}
		}
	}

	private void writeUsernames(CompactWriter writer) throws IOException
	{
		for (OsmEntity element : elements) {
			OsmMetadata metadata = element.getMetadata();
			if (metadata != null) {
				String user = metadata.getUser();
				int index = stringPoolUsernames.getId(user);
				writer.writeVariableLengthUnsignedInteger(index);
			}
		}
	}

}

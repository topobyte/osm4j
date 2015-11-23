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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.tbo.data.StringPool;
import de.topobyte.osm4j.tbo.data.StringPoolBuilder;

public class RelationBatch extends EntityBatch<OsmRelation>
{

	protected StringPool stringPoolMembers;

	public RelationBatch(boolean writeMetadata)
	{
		super(writeMetadata);
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CompactWriter bwriter = new OutputStreamCompactWriter(baos);

		writeTagStringPool(bwriter);
		writeAndReset(writer, baos);

		writeMemberStringPool(bwriter);
		writeAndReset(writer, baos);

		writeIds(bwriter);
		writeAndReset(writer, baos);

		writeMembers(bwriter);
		writeAndReset(writer, baos);

		writeTags(bwriter);
		writeAndReset(writer, baos);

		writeMetadata(bwriter);
		writeAndReset(writer, baos);
	}

	private long midOffset = 0;

	private void writeMembers(CompactWriter writer) throws IOException
	{
		for (OsmRelation relation : elements) {
			int nMembers = relation.getNumberOfMembers();

			writer.writeVariableLengthUnsignedInteger(nMembers);
			for (int i = 0; i < nMembers; i++) {
				OsmRelationMember member = relation.getMember(i);
				long mid = member.getId();
				EntityType type = member.getType();
				int t = EntityTypeHelper.getByte(type);
				int index = stringPoolMembers.getId(member.getRole());
				writer.writeByte(t);
				writer.writeVariableLengthSignedInteger(mid - midOffset);
				writer.writeVariableLengthUnsignedInteger(index);
				midOffset = mid;
			}
		}
	}

	public void writeMemberStringPool(CompactWriter writer) throws IOException
	{
		StringPoolBuilder poolBuilder = new StringPoolBuilder();
		for (OsmRelation object : elements) {
			// add roles
			int nMembers = object.getNumberOfMembers();
			for (int i = 0; i < nMembers; i++) {
				OsmRelationMember member = object.getMember(i);
				poolBuilder.add(member.getRole());
			}
		}
		stringPoolMembers = poolBuilder.buildStringPool();

		writePool(writer, stringPoolMembers);
	}

	@Override
	public void clear()
	{
		super.clear();
		midOffset = 0;
	}

}

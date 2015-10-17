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

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.tbo.io.CompactWriter;

public class RelationBag extends EntityBag
{

	private List<OsmRelation> relations;

	public RelationBag(int batchSize)
	{
		relations = new ArrayList<OsmRelation>(batchSize);
	}

	public void put(OsmRelation relation)
	{
		relations.add(relation);
	}

	@Override
	public void write(CompactWriter writer) throws IOException
	{
		super.writeRelations(writer, relations);
		for (OsmRelation relation : relations) {
			write(writer, relation);
		}
	}

	private long idOffset = 0;
	private long midOffset = 0;

	private void write(CompactWriter writer, OsmRelation relation)
			throws IOException
	{
		long id = relation.getId();
		int nMembers = relation.getNumberOfMembers();

		writer.writeVariableLengthSignedInteger(id - idOffset);
		idOffset = id;

		writer.writeVariableLengthSignedInteger(nMembers);
		for (int i = 0; i < nMembers; i++) {
			OsmRelationMember member = relation.getMember(i);
			long mid = member.getId();
			EntityType type = member.getType();
			int t = EntityTypeHelper.getByte(type);
			int index = stringPool.getId(member.getRole());
			writer.writeByte(t);
			writer.writeVariableLengthSignedInteger(mid - midOffset);
			writer.writeVariableLengthSignedInteger(index);
			midOffset = mid;
		}

		writeTags(writer, relation);
	}

	public void clear()
	{
		relations.clear();
		idOffset = 0;
		midOffset = 0;
	}

}

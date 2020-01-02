// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.edit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;

public class TestCreateRelation
{

	public static void main(String[] args)
			throws IOException, URISyntaxException, ParserConfigurationException
	{
		Api api = TestUtil.createApi();
		Changeset changeset = api.createChangeset();
		System.out.println("created changeset " + changeset.getId());

		List<OsmRelationMember> members = new ArrayList<>();
		members.add(new RelationMember(1001, EntityType.Node, "center"));
		members.add(new RelationMember(2001, EntityType.Way, "outer"));
		members.add(new RelationMember(2002, EntityType.Way, "outer"));
		members.add(new RelationMember(2004, EntityType.Way, "outer"));
		members.add(new RelationMember(101, EntityType.Relation, "something"));
		members.add(new RelationMember(102, EntityType.Relation, null));

		List<OsmTag> tags = new ArrayList<>();
		tags.add(new Tag("boundary", "administrative"));

		Relation relation = new Relation(0, members, tags);

		long id = api.createRelation(changeset, relation);
		System.out.println("created relation " + id);
		boolean success = api.closeChangeset(changeset);
		System.out.println("closed? " + success);
	}

}

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

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;

public class TestCreateWay
{

	public static void main(String[] args)
			throws IOException, URISyntaxException, ParserConfigurationException
	{
		Api api = TestUtil.createApi();
		Changeset changeset = api.createChangeset();
		System.out.println("created changeset " + changeset.getId());

		TLongList nodes = new TLongArrayList();
		nodes.add(1001);
		nodes.add(1002);
		nodes.add(1003);

		List<OsmTag> tags = new ArrayList<>();
		tags.add(new Tag("highway", "residential"));

		Way way = new Way(0, nodes, tags);

		long id = api.createWay(changeset, way);
		System.out.println("created way " + id);
		boolean success = api.closeChangeset(changeset);
		System.out.println("closed? " + success);
	}

}

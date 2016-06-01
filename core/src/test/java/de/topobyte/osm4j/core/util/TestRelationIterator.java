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

package de.topobyte.osm4j.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import de.topobyte.osm4j.core.EqualityUtil;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;

public class TestRelationIterator extends BaseTestIterators
{

	@Test
	public void test() throws IOException
	{
		InMemoryListDataSet data = ListDataSetLoader.read(createInput()
				.createIterator(true, true), true, true, true);

		OsmIteratorInput input = createInput().createIterator(true, true);
		RelationIterator relationIterator = new RelationIterator(
				input.getIterator());
		List<OsmRelation> relations = Lists.newArrayList(relationIterator
				.iterator());
		input.close();

		assertEquals(data.getRelations().size(), relations.size());

		for (int i = 0; i < relations.size(); i++) {
			OsmRelation a = relations.get(i);
			OsmRelation b = data.getRelations().get(i);
			assertTrue(EqualityUtil.equals(a, b));
		}
	}

}

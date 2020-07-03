// Copyright 2018 Sebastian Kuerten
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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStream;

public class OsmContributorHistogram extends AbstractExecutableSingleInputStream
{

	@Override
	protected String getHelpMessage()
	{
		return OsmContributorHistogram.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmContributorHistogram task = new OsmContributorHistogram();
		task.setup(args);

		task.init();

		task.run();

		task.finish();
	}

	private Multiset<Long> counter = HashMultiset.create();
	private Multimap<Long, String> map = HashMultimap.create();

	private void run() throws IOException
	{
		long total = 0;

		OsmIterator iterator = createIterator();
		while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			OsmEntity entity = entityContainer.getEntity();
			OsmMetadata metadata = entity.getMetadata();

			total++;

			if (metadata == null) {
				continue;
			}

			long uid = metadata.getUid();
			String username = metadata.getUser();
			counter.add(uid);

			if (!map.containsEntry(uid, username)) {
				map.put(uid, username);
			}
		}

		if (counter.isEmpty()) {
			System.out.println("No metadata found");
			return;
		}

		long sum = 0;

		ImmutableMultiset<Long> histogram = Multisets
				.copyHighestCountFirst(counter);

		long first = histogram.iterator().next();
		int firstCount = histogram.count(first);
		int firstLength = String.format("%d", firstCount).length();
		String pattern = String.format("[%%6.2f%%%%] %%%dd: %%d (%%s)",
				firstLength);

		for (long id : histogram.elementSet()) {
			int count = histogram.count(id);
			sum += count;
			double done = sum / (double) total;
			List<String> names = new ArrayList<>(map.get(id));
			Collections.sort(names);
			System.out.println(String.format(pattern, done * 100, count, id,
					Joiner.on(",").join(names)));
		}

		finish();
	}

}

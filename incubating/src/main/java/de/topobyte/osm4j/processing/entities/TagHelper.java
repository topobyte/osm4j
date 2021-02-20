// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.processing.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public class TagHelper
{

	public static Map<String, String> commonTagsEntities(
			List<? extends OsmEntity> list)
	{
		List<Map<String, String>> tagsList = new ArrayList<>();
		for (OsmEntity entity : list) {
			tagsList.add(OsmModelUtil.getTagsAsMap(entity));
		}
		return commonTags(tagsList);
	}

	public static Map<String, String> commonTags(
			List<Map<String, String>> tagsList)
	{
		Map<String, String> result = new HashMap<>();
		result.putAll(tagsList.get(0));

		for (int i = 1; i < tagsList.size(); i++) {
			Map<String, String> tags = tagsList.get(i);
			result = commonTags(result, tags);
		}

		return result;
	}

	public static Map<String, String> commonTags(Map<String, String> a,
			Map<String, String> b)
	{
		Map<String, String> results = new HashMap<>();
		for (String key : a.keySet()) {
			if (!b.containsKey(key)) {
				continue;
			}
			String valueA = a.get(key);
			String valueB = b.get(key);
			if (valueA.equals(valueB)) {
				results.put(key, valueA);
			}
		}
		return results;
	}

}

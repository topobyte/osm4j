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

package de.topobyte.osm4j.extra.idextract;

import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class Extractors
{

	public static Extractor create(EntityType type,
			List<ExtractionItem> extractionItems, OsmOutputConfig outputConfig,
			boolean lowMemory, OsmIterator iterator, boolean threaded)
	{
		if (!threaded) {
			return new SimpleExtractor(type, extractionItems, outputConfig,
					lowMemory, iterator);
		} else {
			return new ThreadedExtractor(type, extractionItems, outputConfig,
					lowMemory, iterator);
		}
	}

}

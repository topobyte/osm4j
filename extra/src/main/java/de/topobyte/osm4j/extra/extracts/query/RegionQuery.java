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

package de.topobyte.osm4j.extra.extracts.query;

import java.nio.file.Path;

import com.vividsolutions.jts.geom.Geometry;

import de.topobyte.jts.utils.predicate.PredicateEvaluatorPrepared;
import de.topobyte.osm4j.extra.extracts.BatchFileNames;
import de.topobyte.osm4j.extra.extracts.ExtractionPaths;
import de.topobyte.osm4j.extra.extracts.TreeFileNames;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class RegionQuery extends Query
{

	public RegionQuery(Geometry region, Path pathOutput, Path pathTmp,
			ExtractionPaths paths, TreeFileNames treeNames,
			BatchFileNames relationNames, FileFormat inputFormat,
			OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig, boolean keepTmp,
			boolean fastRelationTests)
	{
		super(pathOutput, pathTmp, paths, treeNames, relationNames, region
				.getEnvelopeInternal(), new PredicateEvaluatorPrepared(region),
				inputFormat, outputConfigIntermediate, outputConfig, keepTmp,
				fastRelationTests);
	}

}

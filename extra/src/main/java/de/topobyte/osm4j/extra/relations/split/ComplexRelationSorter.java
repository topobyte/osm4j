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

package de.topobyte.osm4j.extra.relations.split;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.OsmOutput;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class ComplexRelationSorter extends RelationSorterBase
{

	public ComplexRelationSorter(Path pathInputBboxes, Path pathOutput,
			String fileNamesRelations, OsmIteratorInputFactory iteratorFactory,
			FileFormat outputFormat, boolean writeMetadata,
			PbfConfig pbfConfig, TboConfig tboConfig)
	{
		super(pathInputBboxes, pathOutput, fileNamesRelations, iteratorFactory,
				outputFormat, writeMetadata, pbfConfig, tboConfig);
	}

	public void execute() throws IOException
	{
		ensureOutputDirectory();

		createBatchOutputs();

		ComplexRelationGrouper grouper = new ComplexRelationGrouper(
				iteratorFactory);
		grouper.buildGroups();
		grouper.readGroupRelations(writeMetadata);

		List<Group> groups = grouper.getGroups();
		TLongObjectMap<OsmRelation> groupRelations = grouper
				.getGroupRelations();

		int relationCount = 0;
		for (Group group : groups) {
			long representative = lowestId(group.getRelationIds());
			if (!idToBatch.containsKey(representative)) {
				System.out.println("not available: " + representative);
			}
			int batchNum = idToBatch.get(representative);
			OsmOutput osmOutput = outputs.get(batchNum);

			TLongSet ids = group.getRelationIds();
			for (long id : ids.toArray()) {
				OsmRelation relation = groupRelations.get(id);
				osmOutput.getOsmOutput().write(relation);
				relationCount++;
			}
		}

		closeOutputs();

		System.out.println(String.format("Wrote %s relations in %d batches",
				format.format(relationCount), batches.size()));
	}

	private long lowestId(TLongSet ids)
	{
		long lowest = Long.MAX_VALUE;
		TLongIterator iterator = ids.iterator();
		while (iterator.hasNext()) {
			lowest = Math.min(lowest, iterator.next());
		}
		return lowest;
	}

}

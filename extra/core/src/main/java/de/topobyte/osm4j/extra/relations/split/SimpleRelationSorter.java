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

import java.io.IOException;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.util.RelationIterator;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class SimpleRelationSorter extends RelationSorterBase
{

	public SimpleRelationSorter(Path pathInputBboxes, Path pathOutput,
			String fileNamesRelations, OsmIteratorInputFactory iteratorFactory,
			OsmOutputConfig outputConfig, Path pathOutputBboxList,
			int maxMembers)
	{
		super(pathInputBboxes, pathOutput, fileNamesRelations, iteratorFactory,
				outputConfig, pathOutputBboxList, maxMembers);
	}

	public void execute() throws IOException
	{
		ensureOutputDirectory();

		createBboxOutput();
		createBatchOutputs();

		OsmIteratorInput iteratorInput = iteratorFactory.createIterator(true,
				outputConfig.isWriteMetadata());
		RelationIterator relations = new RelationIterator(
				iteratorInput.getIterator());

		int relationCount = 0;
		for (OsmRelation relation : relations) {
			if (!idToBatch.containsKey(relation.getId())) {
				System.out.println("not available: " + relation.getId());
			}
			int batchNum = idToBatch.get(relation.getId());
			OsmStreamOutput osmOutput = outputs.get(batchNum);
			osmOutput.getOsmOutput().write(relation);
			relationCount++;
		}

		iteratorInput.close();
		closeOutputs();

		System.out.println(String.format("Wrote %s relations in %d batches",
				format.format(relationCount), batches.size()));
	}

}

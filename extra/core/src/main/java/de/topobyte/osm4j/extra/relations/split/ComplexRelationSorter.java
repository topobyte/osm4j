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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.sort.MemorySort;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.set.TLongSet;

public class ComplexRelationSorter extends RelationSorterBase
{

	private String fileNamesRelationsSorted;
	private boolean keepUnsortedRelations;

	public ComplexRelationSorter(Path pathInputBboxes, Path pathOutput,
			String fileNamesRelationsUnsorted, String fileNamesRelations,
			OsmIteratorInputFactory iteratorFactory,
			OsmOutputConfig outputConfig, Path pathOutputBboxList,
			int maxMembers, boolean keepUnsortedRelations)
	{
		super(pathInputBboxes, pathOutput, fileNamesRelationsUnsorted,
				iteratorFactory, outputConfig, pathOutputBboxList, maxMembers);
		this.fileNamesRelationsSorted = fileNamesRelations;
		this.keepUnsortedRelations = keepUnsortedRelations;
	}

	public void execute() throws IOException
	{
		ensureOutputDirectory();

		createBboxOutput();
		createBatchOutputs();

		ComplexRelationGrouper grouper = new ComplexRelationGrouper(
				iteratorFactory, true, false);
		grouper.buildGroups();
		grouper.readGroupRelations(outputConfig.isWriteMetadata());

		List<Group> groups = grouper.getGroups();
		TLongObjectMap<OsmRelation> groupRelations = grouper
				.getGroupRelations();

		int relationCount = 0;
		for (Group group : groups) {
			long representative = group.getStart();
			if (!idToBatch.containsKey(representative)) {
				System.out.println("not available: " + representative);
			}
			int batchNum = idToBatch.get(representative);
			OsmStreamOutput osmOutput = outputs.get(batchNum);

			TLongSet ids = group.getRelationIds();
			for (long id : ids.toArray()) {
				OsmRelation relation = groupRelations.get(id);
				if (relation == null) {
					System.out.println("relation not found: " + id);
					continue;
				}
				osmOutput.getOsmOutput().write(relation);
				relationCount++;
			}
		}

		closeOutputs();

		System.out.println(String.format("Wrote %s relations in %d batches",
				format.format(relationCount), batches.size()));

		boolean useMetadata = outputConfig.isWriteMetadata();

		for (int i = 0; i < batches.size(); i++) {
			int id = i + 1;
			System.out.println("sorting " + id);
			Path pathUnsorted = batchFile(id, fileNamesRelations);
			Path pathSorted = batchFile(id, fileNamesRelationsSorted);

			InputStream input = StreamUtil.bufferedInputStream(pathUnsorted);
			OutputStream output = StreamUtil.bufferedOutputStream(pathSorted);

			OsmIterator osmInput = OsmIoUtils.setupOsmIterator(input,
					outputConfig.getFileFormat(), useMetadata);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputConfig);

			MemorySort memorySort = new MemorySort(osmOutput, osmInput);
			memorySort.setIgnoreDuplicates(true);
			memorySort.run();

			output.close();
			input.close();

			if (!keepUnsortedRelations) {
				Files.delete(pathUnsorted);
			}
		}
	}

}

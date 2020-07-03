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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.OutputUtil;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGroupUtil;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class ComplexRelationSplitter
{

	final static Logger logger = LoggerFactory
			.getLogger(ComplexRelationSplitter.class);

	private int maxMembers = 100 * 1000;

	private Path pathOutput;
	private String fileNamesRelations;
	private OsmIteratorInputFactory iteratorFactory;

	private OsmOutputConfig outputConfig;

	private List<Group> groups;
	private TLongObjectMap<OsmRelation> groupRelations;

	public ComplexRelationSplitter(Path pathOutput, String fileNamesRelations,
			OsmIteratorInputFactory iteratorFactory,
			OsmOutputConfig outputConfig)
	{
		this.pathOutput = pathOutput;
		this.fileNamesRelations = fileNamesRelations;
		this.iteratorFactory = iteratorFactory;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		OutputUtil.ensureOutputDirectory(pathOutput);

		ComplexRelationGrouper grouper = new ComplexRelationGrouper(
				iteratorFactory, false, true);
		grouper.buildGroups();
		grouper.readGroupRelations(outputConfig.isWriteMetadata());

		groups = grouper.getGroups();
		groupRelations = grouper.getGroupRelations();

		determineGroupSizes();

		sortGroupsBySize();

		processGroupBatches();
	}

	private void determineGroupSizes()
	{
		InMemoryMapDataSet data = new InMemoryMapDataSet();
		data.setRelations(groupRelations);
		for (Group group : groups) {
			group.setNumMembers(RelationGroupUtil.groupSize(group, data));
		}
	}

	private void sortGroupsBySize()
	{
		Collections.sort(groups, new Comparator<Group>() {

			@Override
			public int compare(Group o1, Group o2)
			{
				return Integer.compare(o2.getNumMembers(), o1.getNumMembers());
			}
		});
	}

	private void processGroupBatches() throws IOException
	{
		GroupBatch batch = new GroupBatch(maxMembers);

		batches: while (!groups.isEmpty()) {
			Iterator<Group> iterator = groups.iterator();
			while (iterator.hasNext()) {
				Group group = iterator.next();
				if (!batch.fits(group)) {
					continue;
				}
				iterator.remove();
				batch.add(group);
				if (batch.isFull()) {
					process(batch);
					batch.clear();
					status();
					continue batches;
				}
			}
			process(batch);
			batch.clear();
			status();
		}
	}

	private int batchCount = 0;
	private int relationCount = 0;

	private long start = System.currentTimeMillis();
	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	private void status()
	{
		long now = System.currentTimeMillis();
		long past = now - start;

		double seconds = past / 1000;
		long perSecond = Math.round(relationCount / seconds);

		logger.debug(String.format(
				"Processed: %s relations, time passed: %.2f per second: %s",
				format.format(relationCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(GroupBatch batch) throws IOException
	{
		logger.info(String.format("groups: %d, members: %d",
				batch.getElements().size(), batch.getSize()));

		List<Group> groups = batch.getElements();

		TLongSet batchRelationIds = new TLongHashSet();
		for (Group group : groups) {
			batchRelationIds.addAll(group.getRelationIds());
		}

		List<OsmRelation> relations = new ArrayList<>();
		for (long relationId : batchRelationIds.toArray()) {
			OsmRelation relation = groupRelations.get(relationId);
			if (relation == null) {
				logger.info("relation not found: " + relationId);
				continue;
			}
			relations.add(relation);
		}

		batchCount++;

		String subdirName = String.format("%d", batchCount);
		Path subdir = pathOutput.resolve(subdirName);
		Path path = subdir.resolve(fileNamesRelations);
		Files.createDirectory(subdir);

		OutputStream output = StreamUtil.bufferedOutputStream(path.toFile());
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputConfig);

		for (OsmRelation relation : relations) {
			osmOutput.write(relation);
		}

		osmOutput.complete();
		output.close();

		relationCount += relations.size();
	}

}

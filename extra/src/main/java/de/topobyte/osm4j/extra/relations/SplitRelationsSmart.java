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

package de.topobyte.osm4j.extra.relations;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputFileOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SplitRelationsSmart extends AbstractTaskSingleInputFileOutput
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";

	@Override
	protected String getHelpMessage()
	{
		return SplitRelationsSmart.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitRelationsSmart task = new SplitRelationsSmart();

		task.setup(args);

		task.init();

		task.execute();
	}

	private String pathOutput;

	private Path dirOutput;
	private String fileNamesRelations;

	public SplitRelationsSmart()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
	}

	protected void init() throws IOException
	{
		dirOutput = Paths.get(pathOutput);

		if (!Files.exists(dirOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(dirOutput);
		}
		if (!Files.isDirectory(dirOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (dirOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}
	}

	private int maxMembers = 400 * 1000;

	private RelationGraph relationGraph = new RelationGraph(false, true);
	private List<Group> groups;
	private TLongObjectMap<OsmRelation> groupRelations;

	private void execute() throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				false);
		relationGraph.build(iterator);
		input.close();

		System.out.println("Number of relations without relation members: "
				+ relationGraph.getNumNoChildren());
		System.out.println("Number of relations with relation members: "
				+ relationGraph.getIdsHasChildRelations().size());
		System.out.println("Number of child relations: "
				+ relationGraph.getIdsIsChildRelation().size());

		groups = relationGraph.buildGroups();

		getGroupRelations();

		determineGroupSizes();

		sortGroupsBySize();

		processGroupBatches();
	}

	private void getGroupRelations() throws FileNotFoundException, IOException
	{
		TLongSet idsHasChildRelations = relationGraph.getIdsHasChildRelations();
		TLongSet idsIsChildRelation = relationGraph.getIdsIsChildRelation();

		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);

		groupRelations = new TLongObjectHashMap<>();
		for (EntityContainer container : iterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			if (idsHasChildRelations.contains(relation.getId())
					|| idsIsChildRelation.contains(relation.getId())) {
				groupRelations.put(relation.getId(), relation);
			}
		}

		input.close();
	}

	private void determineGroupSizes()
	{
		for (Group group : groups) {
			group.setNumMembers(groupsize(group));
		}
	}

	private int groupsize(Group group)
	{
		int n = 0;
		for (long member : group.getRelationIds().toArray()) {
			OsmRelation relation = groupRelations.get(member);
			n += relation.getNumberOfMembers();
		}
		return n;
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

		System.out.println(String.format(
				"Processed: %s relations, time passed: %.2f per second: %s",
				format.format(relationCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(GroupBatch batch) throws IOException
	{
		System.out.println(String.format("groups: %d, members: %d", batch
				.getGroups().size(), batch.getNumMembers()));

		List<Group> groups = batch.getGroups();

		TLongSet batchRelationIds = new TLongHashSet();
		for (Group group : groups) {
			batchRelationIds.addAll(group.getRelationIds());
		}

		List<OsmRelation> relations = new ArrayList<>();
		for (long relationId : batchRelationIds.toArray()) {
			relations.add(groupRelations.get(relationId));
		}

		batchCount++;

		String subdirName = String.format("%d", batchCount);
		Path subdir = dirOutput.resolve(subdirName);
		Path path = subdir.resolve(fileNamesRelations);
		Files.createDirectory(subdir);

		OutputStream output = StreamUtil.bufferedOutputStream(path.toFile());
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputFormat, writeMetadata, pbfConfig, tboConfig);

		for (OsmRelation relation : relations) {
			osmOutput.write(relation);
		}

		osmOutput.complete();
		output.close();

		relationCount += relations.size();
	}

}

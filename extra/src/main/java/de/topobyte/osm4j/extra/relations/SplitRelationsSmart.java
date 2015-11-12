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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.topobyte.adt.graph.Graph;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
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

	private int numNoChildren = 0;
	private Graph<Long> graph = new Graph<>();
	private TLongSet idsHasChildRelations = new TLongHashSet();
	private TLongSet idsIsChildRelation = new TLongHashSet();

	private List<Group> groups = new LinkedList<>();
	private TLongObjectMap<OsmRelation> groupRelations;

	private TLongSet processed;

	private void execute() throws IOException
	{
		buildGraph();

		System.out.println("Number of relations without relation members: "
				+ numNoChildren);
		System.out.println("Number of relations with relation members: "
				+ idsHasChildRelations.size());
		System.out.println("Number of child relations: "
				+ idsIsChildRelation.size());

		buildGroups();

		getGroupRelations();

		determineGroupSizes();

		sortGroupsBySize();

		processGroupBatches();

		groupRelations.clear();
		graph = null;
		processed = new TLongHashSet();
		processed.addAll(idsHasChildRelations);
		idsHasChildRelations.clear();
		processed.addAll(idsIsChildRelation);
		idsIsChildRelation.clear();

		processRemaining();
	}

	private void buildGraph() throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				false);
		for (EntityContainer container : iterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			boolean hasChildRelations = false;
			TLongList childRelationMembers = new TLongArrayList();
			for (OsmRelationMember member : OsmModelUtil
					.membersAsList(relation)) {
				if (member.getType() == EntityType.Relation) {
					hasChildRelations = true;
					idsIsChildRelation.add(member.getId());
					childRelationMembers.add(member.getId());
				}
			}
			if (hasChildRelations) {
				long id = relation.getId();
				idsHasChildRelations.add(id);
				graph.addNode(id);
				for (long member : childRelationMembers.toArray()) {
					if (!graph.getNodes().contains(member)) {
						graph.addNode(member);
					}
					graph.addEdge(id, member);
				}
			} else {
				numNoChildren++;
			}
		}
	}

	private void buildGroups()
	{
		TLongSet starts = new TLongHashSet();
		Collection<Long> ids = graph.getNodes();
		for (long id : ids) {
			if (graph.getEdgesIn(id).isEmpty()) {
				starts.add(id);
			}
		}
		System.out.println("Number of start relations: " + starts.size());
		for (long start : starts.toArray()) {
			build(start);
		}
	}

	private void build(long start)
	{
		TLongSet group = new TLongHashSet();
		group.add(start);

		TLongSet left = new TLongHashSet();
		left.addAll(graph.getEdgesOut(start));

		while (!left.isEmpty()) {
			// System.out.println("left: " + left.size());
			TLongIterator iterator = left.iterator();
			long next = iterator.next();
			// System.out.println("got: " + next);
			iterator.remove();

			if (group.contains(next)) {
				continue;
			}
			group.add(next);
			Set<Long> out = graph.getEdgesOut(next);
			// System.out.println("adding: " + out);
			left.addAll(out);
		}

		groups.add(new Group(start, group));
	}

	private void getGroupRelations() throws FileNotFoundException, IOException
	{
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

	private void processRemaining() throws IOException
	{
		RelationBatch batch = new RelationBatch(maxMembers);

		InputStream input = StreamUtil.bufferedInputStream(getInputFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, inputFormat,
				writeMetadata);

		for (EntityContainer container : iterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			if (relation.getNumberOfMembers() == 0) {
				continue;
			}
			if (processed.contains(relation.getId())) {
				continue;
			}
			if (batch.fits(relation)) {
				batch.add(relation);
			} else {
				process(batch);
				status();
				batch.clear();
				batch.add(relation);
			}
		}
		if (!batch.getRelations().isEmpty()) {
			process(batch);
			status();
			batch.clear();
		}

		input.close();
	}

	private void process(RelationBatch batch) throws IOException
	{
		List<OsmRelation> relations = batch.getRelations();

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

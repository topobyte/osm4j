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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.FileNotFoundException;
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

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.relations.Group;
import de.topobyte.osm4j.extra.relations.RelationGraph;
import de.topobyte.osm4j.extra.relations.RelationIterator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmIteratorFactory;
import de.topobyte.osm4j.utils.OsmIteratorInput;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class ComplexRelationSplitter
{

	private int maxMembers = 100 * 1000;

	private Path pathOutput;
	private String fileNamesRelations;
	private OsmIteratorFactory iteratorFactory;

	private FileFormat outputFormat;
	private boolean writeMetadata;

	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	public ComplexRelationSplitter(Path pathOutput, String fileNamesRelations,
			OsmIteratorFactory iteratorFactory, FileFormat outputFormat,
			boolean writeMetadata, PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.pathOutput = pathOutput;
		this.fileNamesRelations = fileNamesRelations;
		this.iteratorFactory = iteratorFactory;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
	}

	private RelationGraph relationGraph = new RelationGraph(false, true);
	private List<Group> groups;
	private TLongObjectMap<OsmRelation> groupRelations;

	public void execute() throws IOException
	{
		if (!Files.exists(pathOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(pathOutput);
		}
		if (!Files.isDirectory(pathOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (pathOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}

		OsmIteratorInput iteratorInput = iteratorFactory.createIterator(false);
		relationGraph.build(iteratorInput.getIterator());
		iteratorInput.close();

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

		OsmIteratorInput iteratorInput = iteratorFactory
				.createIterator(writeMetadata);
		RelationIterator relations = new RelationIterator(
				iteratorInput.getIterator());

		groupRelations = new TLongObjectHashMap<>();
		for (OsmRelation relation : relations) {
			if (idsHasChildRelations.contains(relation.getId())
					|| idsIsChildRelation.contains(relation.getId())) {
				groupRelations.put(relation.getId(), relation);
			}
		}

		iteratorInput.close();
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
				.getElements().size(), batch.getSize()));

		List<Group> groups = batch.getElements();

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
		Path subdir = pathOutput.resolve(subdirName);
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

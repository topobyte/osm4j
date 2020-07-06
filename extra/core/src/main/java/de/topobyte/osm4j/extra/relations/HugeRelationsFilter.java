// Copyright 2020 Sebastian Kuerten
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmInputAccessFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.util.RelationIterator;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class HugeRelationsFilter
{

	final static Logger logger = LoggerFactory
			.getLogger(HugeRelationsFilter.class);

	private OsmInputAccessFactory inputFactory;
	private Path pathOutputHuge;
	private Path pathOutputRemainder;
	private int maxMembers;
	private OsmOutputConfig outputConfig;

	public HugeRelationsFilter(OsmInputAccessFactory inputFactory,
			Path pathOutputHuge, Path pathOutputRemainder, int maxMembers,
			OsmOutputConfig outputConfig)
	{
		this.inputFactory = inputFactory;
		this.pathOutputHuge = pathOutputHuge;
		this.pathOutputRemainder = pathOutputRemainder;
		this.maxMembers = maxMembers;
		this.outputConfig = outputConfig;
	}

	private TLongSet idsHuge = new TLongHashSet();

	public void execute() throws IOException
	{
		findHugeRelations();

		separateRelations();
	}

	private void findHugeRelations() throws IOException
	{
		OsmIteratorInput input = inputFactory.createIterator(false, false);

		OsmIterator iterator = input.getIterator();
		// TODO: for a file with nodes and ways included this is bad because it
		// loads them into memory, too
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		RelationGraph graph = new RelationGraph(true, false);
		graph.build(data.getRelations().valueCollection());
		List<Group> groups = graph.buildGroups();

		logger.info(
				String.format("Number of relation groups: %d", groups.size()));

		for (Group group : groups) {
			group.setNumMembers(RelationGroupUtil.groupSize(group, data));
		}

		for (Group group : groups) {
			if (group.getNumMembers() > maxMembers) {
				idsHuge.add(group.getStart());
				findStartRelationsInRestOfGroup(group,
						data.getRelations().valueCollection(), data);
			}
		}

		logger.info(
				String.format("Number of huge relations: %d", idsHuge.size()));

		input.close();
	}

	private void findStartRelationsInRestOfGroup(Group huge,
			Collection<OsmRelation> data, InMemoryMapDataSet provider)
			throws IOException
	{
		TLongSet ids = new TLongHashSet(huge.getRelationIds());
		ids.remove(huge.getStart());

		List<OsmRelation> relations = filter(data, ids);

		RelationGraph graph = new RelationGraph(true, false);
		graph.build(relations);
		List<Group> groups = graph.buildGroups();

		for (Group group : groups) {
			group.setNumMembers(RelationGroupUtil.groupSize(group, provider));
		}

		for (Group group : groups) {
			if (group.getNumMembers() > maxMembers) {
				idsHuge.add(group.getStart());
				findStartRelationsInRestOfGroup(group, data, provider);
			}
		}
	}

	private List<OsmRelation> filter(Collection<OsmRelation> relations,
			TLongSet ids)
	{
		List<OsmRelation> filtered = new ArrayList<>();
		for (OsmRelation relation : relations) {
			if (ids.contains(relation.getId())) {
				filtered.add(relation);
			}
		}
		return filtered;
	}

	private void separateRelations() throws IOException
	{
		OutputStream outHuge = StreamUtil.bufferedOutputStream(pathOutputHuge);
		OutputStream outRemainder = StreamUtil
				.bufferedOutputStream(pathOutputRemainder);

		OsmOutputStream osmOutputHuge = OsmIoUtils.setupOsmOutput(outHuge,
				outputConfig);
		OsmOutputStream osmOutputRemainder = OsmIoUtils
				.setupOsmOutput(outRemainder, outputConfig);

		OsmIteratorInput input = inputFactory.createIterator(true,
				outputConfig.isWriteMetadata());

		for (OsmRelation relation : new RelationIterator(input.getIterator())) {
			long id = relation.getId();
			if (idsHuge.contains(id)) {
				osmOutputHuge.write(relation);
			} else {
				osmOutputRemainder.write(relation);
			}
		}

		osmOutputHuge.complete();
		osmOutputRemainder.complete();

		outHuge.close();
		outRemainder.close();

		input.close();
	}

}

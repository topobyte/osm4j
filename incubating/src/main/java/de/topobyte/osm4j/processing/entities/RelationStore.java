// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.processing.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;

public class RelationStore
{

	// Contains a mapping from way ids to lists of relations. A way id maps to a
	// list of relations that it appears as outer way in.
	private ListMultimap<Long, OsmRelation> wayIdToRelationOuter = ArrayListMultimap
			.create();
	// same thing for inner ways
	private ListMultimap<Long, OsmRelation> wayIdToRelationInner = ArrayListMultimap
			.create();

	// a map from relation ids to a possible replacement for the relation
	// identified by that.
	private TLongObjectMap<OsmRelation> replacements = new TLongObjectHashMap<>();

	// a map storing metadata for processed relations
	private TLongObjectMap<RelationData> relationData = new TLongObjectHashMap<>();

	private class RelationData
	{
		private int nOuter = 0;
		private int nInner = 0;
	}

	public void put(OsmRelation relation)
	{
		RelationData data = new RelationData();
		relationData.put(relation.getId(), data);

		// process members
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (!(member.getType() == EntityType.Way)) {
				continue;
			}
			long memberId = member.getId();
			if (member.getRole().equals("inner")) {
				data.nInner++;
				wayIdToRelationInner.put(memberId, relation);
			}
			if (member.getRole().equals("outer")) {
				data.nOuter++;
				wayIdToRelationOuter.put(memberId, relation);
			}
		}
	}

	public TLongObjectMap<RelationData> getRelationData()
	{
		return relationData;
	}

	public boolean isOuter(OsmWay way)
	{
		return wayIdToRelationOuter.containsKey(way.getId());
	}

	public boolean isInner(OsmWay way)
	{
		return wayIdToRelationInner.containsKey(way.getId());
	}

	public Collection<OsmRelation> getInnerRelations(OsmWay way)
	{
		return wayIdToRelationInner.get(way.getId());
	}

	public Collection<OsmRelation> getOuterRelations(OsmWay way)
	{
		return wayIdToRelationOuter.get(way.getId());
	}

	public int getNumberOfOuterWays(OsmRelation relation)
	{
		return relationData.get(relation.getId()).nOuter;
	}

	public int getNumberOfInnerWays(OsmRelation relation)
	{
		return relationData.get(relation.getId()).nInner;
	}

	public OsmRelation getReplacement(OsmRelation relation)
	{
		OsmRelation replacement = replacements.get(relation.getId());
		if (replacement != null) {
			return replacement;
		}
		return relation;
	}

	public void applyTags(Map<String, String> tags, OsmRelation relation)
	{
		OsmRelation replacement = replacements.get(relation.getId());
		if (replacement != null) {
			relation = replacement;
		}

		Map<String, String> relationTags = OsmModelUtil.getTagsAsMap(relation);

		// System.out.println("original tags: " + relationTags);
		// System.out.println("additional tags: " + tags);

		for (String key : tags.keySet()) {
			if (!relationTags.containsKey(key)) {
				relationTags.put(key, tags.get(key));
			}
		}

		List<OsmTag> newTags = new ArrayList<>();
		for (String key : relationTags.keySet()) {
			String value = relationTags.get(key);
			newTags.add(new Tag(key, value));
		}

		OsmRelation newRelation = new Relation(relation.getId(),
				OsmModelUtil.membersAsList(relation), newTags);

		replacements.put(relation.getId(), newRelation);
	}

	public void subtractTags(Map<String, String> tags,
			Map<String, String> relationTags)
	{
		for (String key : relationTags.keySet()) {
			if (!tags.containsKey(key)) {
				continue;
			}
			String value = tags.get(key);
			String relationValue = relationTags.get(key);
			if (value.equals(relationValue)) {
				tags.remove(key);
			}
		}
	}

}

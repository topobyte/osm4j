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
//
//
// This files is based on a file from Osmosis. The original file contained this
// copyright notice:
//
// This software is released into the Public Domain. See copying.txt for details.
//
//
// And the mentioned copying.txt states:
//
// Osmosis is placed into the public domain and where this is not legally
// possible everybody is granted a perpetual, irrevocable license to use
// this work for any purpose whatsoever.
//
// DISCLAIMERS
// By making Osmosis publicly available, it is hoped that users will find the
// software useful. However:
//   * Osmosis comes without any warranty, to the extent permitted by
//     applicable law.
//   * Unless required by applicable law, no liability will be accepted by
// the authors and distributors of this software for any damages caused
// as a result of its use.

package de.topobyte.osm4j.pbf;

import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.DenseInfo;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Info;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Relation.MemberType;
import crosby.binary.Osmformat.Way;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;

public class PbfParser extends BinaryParser
{

	private final OsmHandler handler;
	private boolean fetchMetadata;

	public PbfParser(OsmHandler handler, boolean fetchMetadata)
	{
		this.handler = handler;
		this.fetchMetadata = fetchMetadata;
	}

	@Override
	public void complete() throws IOException
	{
		handler.complete();
	}

	@Override
	protected void parse(HeaderBlock header)
	{
	}

	@Override
	protected void parseNodes(List<Node> nodes) throws IOException
	{
		for (Osmformat.Node i : nodes) {
			long id = i.getId();
			double latf = parseLat(i.getLat()), lonf = parseLon(i.getLon());
			List<OsmTag> tags = new ArrayList<OsmTag>();

			OsmMetadata metadata = null;
			if (fetchMetadata) {
				Info info = i.getInfo();
				metadata = convertMetadata(info);
			}
			de.topobyte.osm4j.core.model.impl.Node node = new de.topobyte.osm4j.core.model.impl.Node(
					id, lonf, latf, tags, metadata);

			for (int j = 0; j < i.getKeysCount(); j++) {
				tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i
						.getVals(j))));
			}
			handler.handle(node);
		}
	}

	@Override
	protected void parseDense(DenseNodes nodes) throws IOException
	{
		DenseInfo denseInfo = null;
		if (fetchMetadata && nodes.hasDenseinfo()) {
			denseInfo = nodes.getDenseinfo();
		}

		long id = 0, lat = 0, lon = 0;

		int version = 0, uid = 0, userSid = 0;
		long timestamp = 0, changeset = 0;

		int j = 0; // Index into the keysvals array.

		for (int i = 0; i < nodes.getIdCount(); i++) {

			id += nodes.getId(i);
			lat += nodes.getLat(i);
			lon += nodes.getLon(i);

			double latf = parseLat(lat), lonf = parseLon(lon);
			List<OsmTag> tags = new ArrayList<OsmTag>();

			OsmMetadata metadata = null;

			if (fetchMetadata && nodes.hasDenseinfo()) {
				version = denseInfo.getVersion(i);
				timestamp += denseInfo.getTimestamp(i);
				uid += denseInfo.getUid(i);
				userSid += denseInfo.getUserSid(i);
				changeset += denseInfo.getChangeset(i);
				metadata = new Metadata(version, timestamp * date_granularity,
						uid, getStringById(userSid), changeset);
			}

			de.topobyte.osm4j.core.model.impl.Node node = new de.topobyte.osm4j.core.model.impl.Node(
					id, lonf, latf, tags, metadata);

			// If empty, assume that nothing here has keys or vals.
			if (nodes.getKeysValsCount() > 0) {
				while (nodes.getKeysVals(j) != 0) {
					int keyid = nodes.getKeysVals(j++);
					int valid = nodes.getKeysVals(j++);
					tags.add(new Tag(getStringById(keyid), getStringById(valid)));
				}
				j++; // Skip over the '0' delimiter.
			}

			handler.handle(node);
		}
	}

	@Override
	protected void parseWays(List<Way> ways) throws IOException
	{
		for (Osmformat.Way i : ways) {

			long id = i.getId();
			TLongArrayList nodes = new TLongArrayList();
			List<OsmTag> tags = new ArrayList<OsmTag>();

			OsmMetadata metadata = null;
			if (fetchMetadata) {
				Info info = i.getInfo();
				metadata = convertMetadata(info);
			}
			de.topobyte.osm4j.core.model.impl.Way way = new de.topobyte.osm4j.core.model.impl.Way(
					id, nodes, tags, metadata);

			for (int j = 0; j < i.getKeysCount(); j++) {
				tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i
						.getVals(j))));
			}

			long lastId = 0;
			for (long j : i.getRefsList()) {
				nodes.add(j + lastId);
				lastId = j + lastId;
			}

			handler.handle(way);
		}
	}

	@Override
	protected void parseRelations(List<Relation> rels) throws IOException
	{
		for (Osmformat.Relation i : rels) {

			long id = i.getId();
			long lastMid = 0;
			List<RelationMember> members = new ArrayList<RelationMember>();
			List<OsmTag> tags = new ArrayList<OsmTag>();

			OsmMetadata metadata = null;
			if (fetchMetadata) {
				Info info = i.getInfo();
				metadata = convertMetadata(info);
			}
			de.topobyte.osm4j.core.model.impl.Relation relation = new de.topobyte.osm4j.core.model.impl.Relation(
					id, members, tags, metadata);

			for (int j = 0; j < i.getKeysCount(); j++) {
				tags.add(new Tag(getStringById(i.getKeys(j)), getStringById(i
						.getVals(j))));
			}

			for (int j = 0; j < i.getMemidsCount(); j++) {
				long mid = lastMid + i.getMemids(j);
				lastMid = mid;
				String role = getStringById(i.getRolesSid(j));
				MemberType type = i.getTypes(j);

				EntityType t = getType(type);

				RelationMember member = new RelationMember(mid, t, role);
				members.add(member);
			}

			handler.handle(relation);
		}
	}

	private EntityType getType(MemberType type)
	{
		switch (type) {
		default:
		case NODE:
			return EntityType.Node;
		case WAY:
			return EntityType.Way;
		case RELATION:
			return EntityType.Relation;
		}
	}

	private OsmMetadata convertMetadata(Info info)
	{
		Metadata metadata = new Metadata(info.getVersion(), getTimestamp(info),
				info.getUid(), getStringById(info.getUserSid()),
				info.getChangeset());
		return metadata;
	}

}

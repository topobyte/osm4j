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

package de.topobyte.osm4j.pbfng.seq;

import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crosby.binary.Osmformat;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;

public class PbfParser extends BlockParser
{

	private OsmHandler handler;
	private boolean fetchMetadata;

	public PbfParser(OsmHandler handler, boolean fetchMetadata)
	{
		this.handler = handler;
		this.fetchMetadata = fetchMetadata;
	}

	private int granularity;
	private long latOffset;
	private long lonOffset;
	private int dateGranularity;
	private String[] strings;

	@Override
	protected void parse(Osmformat.HeaderBlock block)
	{
		// We're currently ignoring the header
	}

	@Override
	protected void parse(Osmformat.PrimitiveBlock block) throws IOException
	{
		Osmformat.StringTable stringTable = block.getStringtable();
		strings = new String[stringTable.getSCount()];

		for (int i = 0; i < strings.length; i++) {
			strings[i] = stringTable.getS(i).toStringUtf8();
		}

		granularity = block.getGranularity();
		latOffset = block.getLatOffset();
		lonOffset = block.getLonOffset();
		dateGranularity = block.getDateGranularity();

		for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
			parseNodes(group.getNodesList());
			parseWays(group.getWaysList());
			parseRelations(group.getRelationsList());
			if (group.hasDense()) {
				parseDense(group.getDense());
			}
		}
	}

	/**
	 * Convert a latitude value stored in a protobuf into a double, compensating
	 * for granularity and latitude offset
	 */
	protected double parseLat(long degree)
	{
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + latOffset) * .000000001;
	}

	/**
	 * Convert a longitude value stored in a protobuf into a double,
	 * compensating for granularity and longitude offset
	 */
	protected double parseLon(long degree)
	{
		// Support non-zero offsets. (We don't currently generate them)
		return (granularity * degree + lonOffset) * .000000001;
	}

	protected long getTimestamp(Osmformat.Info info)
	{
		if (info.hasTimestamp()) {
			return dateGranularity * info.getTimestamp();
		}
		return -1;
	}

	protected void parseNodes(List<Osmformat.Node> nodes) throws IOException
	{
		for (Osmformat.Node n : nodes) {
			long id = n.getId();
			double lat = parseLat(n.getLat());
			double lon = parseLon(n.getLon());

			List<OsmTag> tags = new ArrayList<OsmTag>();
			for (int j = 0; j < n.getKeysCount(); j++) {
				tags.add(new Tag(strings[n.getKeys(j)], strings[n.getVals(j)]));
			}

			OsmMetadata metadata = null;
			if (fetchMetadata && n.hasInfo()) {
				Osmformat.Info info = n.getInfo();
				metadata = convertMetadata(info);
			}

			Node node = new Node(id, lon, lat, tags, metadata);
			handler.handle(node);
		}
	}

	protected void parseDense(Osmformat.DenseNodes nodes) throws IOException
	{
		Osmformat.DenseInfo denseInfo = null;
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
				metadata = new Metadata(version, timestamp * dateGranularity,
						uid, strings[userSid], changeset);
			}

			// If empty, assume that nothing here has keys or vals.
			if (nodes.getKeysValsCount() > 0) {
				while (nodes.getKeysVals(j) != 0) {
					int keyid = nodes.getKeysVals(j++);
					int valid = nodes.getKeysVals(j++);
					tags.add(new Tag(strings[keyid], strings[valid]));
				}
				j++; // Skip over the '0' delimiter.
			}

			Node node = new Node(id, lonf, latf, tags, metadata);
			handler.handle(node);
		}
	}

	protected void parseWays(List<Osmformat.Way> ways) throws IOException
	{
		for (Osmformat.Way w : ways) {
			long id = w.getId();
			TLongArrayList nodes = new TLongArrayList();

			long lastId = 0;
			for (long j : w.getRefsList()) {
				nodes.add(j + lastId);
				lastId = j + lastId;
			}

			List<OsmTag> tags = new ArrayList<OsmTag>();
			for (int j = 0; j < w.getKeysCount(); j++) {
				tags.add(new Tag(strings[w.getKeys(j)], strings[w.getVals(j)]));
			}

			OsmMetadata metadata = null;
			if (fetchMetadata && w.hasInfo()) {
				Osmformat.Info info = w.getInfo();
				metadata = convertMetadata(info);
			}

			Way way = new Way(id, nodes, tags, metadata);
			handler.handle(way);
		}
	}

	protected void parseRelations(List<Osmformat.Relation> rels)
			throws IOException
	{
		for (Osmformat.Relation r : rels) {
			long id = r.getId();
			long lastMid = 0;

			List<OsmTag> tags = new ArrayList<OsmTag>();
			for (int j = 0; j < r.getKeysCount(); j++) {
				tags.add(new Tag(strings[r.getKeys(j)], strings[r.getVals(j)]));
			}

			List<RelationMember> members = new ArrayList<RelationMember>();
			for (int j = 0; j < r.getMemidsCount(); j++) {
				long mid = lastMid + r.getMemids(j);
				lastMid = mid;
				String role = strings[r.getRolesSid(j)];
				Osmformat.Relation.MemberType type = r.getTypes(j);

				EntityType t = getType(type);

				RelationMember member = new RelationMember(mid, t, role);
				members.add(member);
			}

			OsmMetadata metadata = null;
			if (fetchMetadata && r.hasInfo()) {
				Osmformat.Info info = r.getInfo();
				metadata = convertMetadata(info);
			}

			Relation relation = new Relation(id, members, tags, metadata);
			handler.handle(relation);
		}
	}

	private EntityType getType(Osmformat.Relation.MemberType type)
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

	private OsmMetadata convertMetadata(Osmformat.Info info)
	{
		Metadata metadata = new Metadata(info.getVersion(), getTimestamp(info),
				info.getUid(), strings[info.getUserSid()], info.getChangeset());
		return metadata;
	}

}

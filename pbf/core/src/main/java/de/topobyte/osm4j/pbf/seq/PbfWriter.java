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

package de.topobyte.osm4j.pbf.seq;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.protobuf.ByteString;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.pbf.Compression;
import de.topobyte.osm4j.pbf.Constants;
import de.topobyte.osm4j.pbf.protobuf.Osmformat;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.HeaderBlock;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.PrimitiveBlock;
import de.topobyte.osm4j.pbf.protobuf.Osmformat.PrimitiveGroup;
import de.topobyte.osm4j.pbf.util.PbfUtil;
import de.topobyte.osm4j.pbf.util.StringTable;

public class PbfWriter extends BlockWriter implements OsmOutputStream
{

	private boolean writeMetadata;

	private Compression compression = Compression.DEFLATE;

	private boolean useDense = true;
	private int granularity = 100;
	private int dateGranularity = 1000;
	private StringTable stringTable = new StringTable();

	private int batchLimit = 4000;

	// The total number of elements currently buffered
	// (nodes + ways + relations)
	private int counter = 0;

	// Buffers for elements
	private List<OsmNode> bufNodes = new ArrayList<>();
	private List<OsmWay> bufWays = new ArrayList<>();
	private List<OsmRelation> bufRelations = new ArrayList<>();

	private boolean headerWritten = false;

	public PbfWriter(OutputStream output, boolean writeMetadata)
	{
		super(output);
		this.writeMetadata = writeMetadata;
	}

	public Compression getCompression()
	{
		return compression;
	}

	public void setCompression(Compression compression)
	{
		this.compression = compression;
	}

	public boolean isUseDense()
	{
		return useDense;
	}

	public void setUseDense(boolean useDense)
	{
		this.useDense = useDense;
	}

	public int getGranularity()
	{
		return granularity;
	}

	public void setGranularity(int granularity)
	{
		this.granularity = granularity;
	}

	public int getDateGranularity()
	{
		return dateGranularity;
	}

	public void setDateGranularity(int dateGranularity)
	{
		this.dateGranularity = dateGranularity;
	}

	public int getBatchLimit()
	{
		return batchLimit;
	}

	public void setBatchLimit(int batchLimit)
	{
		this.batchLimit = batchLimit;
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		if (!headerWritten) {
			writeHeader(bounds);
		}
	}

	private void ensureHeader() throws IOException
	{
		if (!headerWritten) {
			writeHeader(null);
		}
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		bufNodes.add(node);
		incrementCounter();
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		bufWays.add(way);
		incrementCounter();
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		bufRelations.add(relation);
		incrementCounter();
	}

	private void incrementCounter() throws IOException
	{
		if (++counter >= batchLimit) {
			writeBatch();
		}
	}

	@Override
	public void complete() throws IOException
	{
		ensureHeader();

		if (counter > 0) {
			writeBatch();
		}
	}

	private void writeHeader(OsmBounds bounds) throws IOException
	{
		HeaderBlock header = PbfUtil.createHeader(Constants.WRITING_PROGRAM,
				true, bounds);
		ByteString headerData = header.toByteString();
		write(Constants.BLOCK_TYPE_HEADER, null, compression, headerData);
		headerWritten = true;
	}

	private void writeBatch() throws IOException
	{
		ensureHeader();

		Osmformat.PrimitiveBlock.Builder builder = Osmformat.PrimitiveBlock
				.newBuilder();

		// First add all strings to the string table

		addTagsToStringTable(bufNodes);
		addTagsToStringTable(bufWays);
		addTagsToStringTable(bufRelations);

		addMemberRolesToStringTable(bufRelations);

		if (writeMetadata) {
			addUsersToStringTable(bufNodes);
			addUsersToStringTable(bufWays);
			addUsersToStringTable(bufRelations);
		}

		// And build indices
		stringTable.finish();

		// Now build groups and add to block builder

		if (bufNodes.size() > 0) {
			if (useDense) {
				PrimitiveGroup group = serializeDense(bufNodes);
				builder.addPrimitivegroup(group);
			} else {
				PrimitiveGroup group = serializeNonDense(bufNodes);
				builder.addPrimitivegroup(group);
			}
			bufNodes.clear();
		}

		if (bufWays.size() > 0) {
			PrimitiveGroup group = serializeWays(bufWays);
			builder.addPrimitivegroup(group);
			bufWays.clear();
		}

		if (bufRelations.size() > 0) {
			PrimitiveGroup group = serializeRelations(bufRelations);
			builder.addPrimitivegroup(group);
			bufRelations.clear();
		}

		builder.setDateGranularity(dateGranularity);
		builder.setGranularity(granularity);
		builder.setStringtable(stringTable.serialize());

		PrimitiveBlock block = builder.build();
		ByteString data = block.toByteString();

		// Reset counter and string table
		counter = 0;
		stringTable.clear();

		write(Constants.BLOCK_TYPE_DATA, null, compression, data);
	}

	private void addTagsToStringTable(Collection<? extends OsmEntity> entities)
	{
		for (OsmEntity entity : entities) {
			for (int k = 0; k < entity.getNumberOfTags(); k++) {
				OsmTag tag = entity.getTag(k);
				stringTable.incr(tag.getKey());
				stringTable.incr(tag.getValue());
			}
		}
	}

	private void addUsersToStringTable(Collection<? extends OsmEntity> entities)
	{
		for (OsmEntity entity : entities) {
			OsmMetadata metadata = entity.getMetadata();
			if (metadata == null) {
				continue;
			}
			String user = metadata.getUser();
			if (user == null) {
				continue;
			}
			stringTable.incr(user);
		}
	}

	private Osmformat.Info.Builder serializeMetadata(OsmEntity entity)
	{
		Osmformat.Info.Builder b = Osmformat.Info.newBuilder();
		if (writeMetadata) {
			OsmMetadata metadata = entity.getMetadata();
			if (metadata == null) {
				return b;
			}
			if (metadata.getUid() >= 0) {
				b.setUid((int) metadata.getUid());
				b.setUserSid(stringTable.getIndex(metadata.getUser()));
			}
			b.setTimestamp((int) (metadata.getTimestamp() / dateGranularity));
			b.setVersion(metadata.getVersion());
			b.setChangeset(metadata.getChangeset());
			b.setVisible(metadata.isVisible());
		}
		return b;
	}

	private void serializeMetadataDense(Osmformat.DenseInfo.Builder b,
			Collection<? extends OsmEntity> entities)
	{
		long lasttimestamp = 0, lastchangeset = 0;
		int lastuserSid = 0, lastuid = 0;
		for (OsmEntity e : entities) {
			OsmMetadata metadata = e.getMetadata();
			if (metadata == null) {
				metadata = new Metadata(-1, -1, -1, "", -1);
			}
			int uid = (int) metadata.getUid();
			int userSid = stringTable.getIndex(metadata.getUser());
			int timestamp = (int) (metadata.getTimestamp() / dateGranularity);
			int version = metadata.getVersion();
			long changeset = metadata.getChangeset();
			boolean visible = metadata.isVisible();

			b.addVersion(version);
			b.addTimestamp(timestamp - lasttimestamp);
			lasttimestamp = timestamp;
			b.addChangeset(changeset - lastchangeset);
			lastchangeset = changeset;
			b.addUid(uid - lastuid);
			lastuid = uid;
			b.addUserSid(userSid - lastuserSid);
			lastuserSid = userSid;
			b.addVisible(visible);
		}
	}

	private int mapDegrees(double degrees)
	{
		return (int) ((degrees / .0000001) / (granularity / 100));
	}

	private Osmformat.PrimitiveGroup serializeDense(Collection<OsmNode> nodes)
	{
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();

		long lastlat = 0, lastlon = 0, lastid = 0;
		Osmformat.DenseNodes.Builder bi = Osmformat.DenseNodes.newBuilder();

		// Does anything in this block have tags?
		boolean doesBlockHaveTags = false;
		for (OsmNode node : nodes) {
			if (node.getNumberOfTags() != 0) {
				doesBlockHaveTags = true;
				break;
			}
		}

		// Find out if any of the nodes has metadata. If none does, we can omit
		// the metadata completely.
		boolean hasMetadata = false;
		for (OsmNode node : nodes) {
			if (node.getMetadata() != null) {
				hasMetadata = true;
			}
		}

		if (writeMetadata && hasMetadata) {
			Osmformat.DenseInfo.Builder bdi = Osmformat.DenseInfo.newBuilder();
			serializeMetadataDense(bdi, nodes);
			bi.setDenseinfo(bdi);
		}

		for (OsmNode node : nodes) {
			long id = node.getId();
			int lat = mapDegrees(node.getLatitude());
			int lon = mapDegrees(node.getLongitude());
			bi.addId(id - lastid);
			lastid = id;
			bi.addLon(lon - lastlon);
			lastlon = lon;
			bi.addLat(lat - lastlat);
			lastlat = lat;

			// Then we must include tag information.
			if (doesBlockHaveTags) {
				for (int k = 0; k < node.getNumberOfTags(); k++) {
					OsmTag t = node.getTag(k);
					bi.addKeysVals(stringTable.getIndex(t.getKey()));
					bi.addKeysVals(stringTable.getIndex(t.getValue()));
				}
				bi.addKeysVals(0); // Add delimiter.
			}
		}

		builder.setDense(bi);
		return builder.build();
	}

	private Osmformat.PrimitiveGroup serializeNonDense(Collection<OsmNode> nodes)
	{
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();
		for (OsmNode node : nodes) {
			Osmformat.Node.Builder bi = Osmformat.Node.newBuilder();
			bi.setId(node.getId());
			bi.setLon(mapDegrees(node.getLongitude()));
			bi.setLat(mapDegrees(node.getLatitude()));
			for (int k = 0; k < node.getNumberOfTags(); k++) {
				OsmTag t = node.getTag(k);
				bi.addKeys(stringTable.getIndex(t.getKey()));
				bi.addVals(stringTable.getIndex(t.getValue()));
			}
			if (writeMetadata && node.getMetadata() != null) {
				bi.setInfo(serializeMetadata(node));
			}
			builder.addNodes(bi);
		}
		return builder.build();
	}

	private Osmformat.PrimitiveGroup serializeWays(Collection<OsmWay> ways)
	{
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();
		for (OsmWay way : ways) {
			Osmformat.Way.Builder bi = Osmformat.Way.newBuilder();
			bi.setId(way.getId());
			long lastid = 0;
			for (int k = 0; k < way.getNumberOfNodes(); k++) {
				long id = way.getNodeId(k);
				bi.addRefs(id - lastid);
				lastid = id;
			}
			for (int k = 0; k < way.getNumberOfTags(); k++) {
				OsmTag t = way.getTag(k);
				bi.addKeys(stringTable.getIndex(t.getKey()));
				bi.addVals(stringTable.getIndex(t.getValue()));
			}
			if (writeMetadata && way.getMetadata() != null) {
				bi.setInfo(serializeMetadata(way));
			}
			builder.addWays(bi);
		}
		return builder.build();
	}

	private void addMemberRolesToStringTable(Collection<OsmRelation> relations)
	{
		for (OsmRelation relation : relations) {
			for (int k = 0; k < relation.getNumberOfMembers(); k++) {
				OsmRelationMember j = relation.getMember(k);
				stringTable.incr(j.getRole());
			}
		}
	}

	private Osmformat.PrimitiveGroup serializeRelations(
			Collection<OsmRelation> relations)
	{
		Osmformat.PrimitiveGroup.Builder builder = Osmformat.PrimitiveGroup
				.newBuilder();
		for (OsmRelation relation : relations) {
			Osmformat.Relation.Builder bi = Osmformat.Relation.newBuilder();
			bi.setId(relation.getId());
			long lastid = 0;
			for (int k = 0; k < relation.getNumberOfMembers(); k++) {
				OsmRelationMember j = relation.getMember(k);
				long id = j.getId();
				bi.addMemids(id - lastid);
				lastid = id;
				EntityType t = j.getType();
				Osmformat.Relation.MemberType type = getType(t);
				bi.addTypes(type);
				bi.addRolesSid(stringTable.getIndex(j.getRole()));
			}

			for (int k = 0; k < relation.getNumberOfTags(); k++) {
				OsmTag t = relation.getTag(k);
				bi.addKeys(stringTable.getIndex(t.getKey()));
				bi.addVals(stringTable.getIndex(t.getValue()));
			}
			if (writeMetadata && relation.getMetadata() != null) {
				bi.setInfo(serializeMetadata(relation));
			}
			builder.addRelations(bi);
		}
		return builder.build();
	}

	private Osmformat.Relation.MemberType getType(EntityType t)
	{
		switch (t) {
		default:
		case Node:
			return Osmformat.Relation.MemberType.NODE;
		case Way:
			return Osmformat.Relation.MemberType.WAY;
		case Relation:
			return Osmformat.Relation.MemberType.RELATION;
		}
	}

}

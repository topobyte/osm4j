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

package de.topobyte.osm4j.tbo.access;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.topobyte.compactio.CompactReader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmMetadata;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.core.model.impl.Entity;
import de.topobyte.osm4j.core.model.impl.Metadata;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.tbo.data.BlockMetadataInfo;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.writerhelper.EntityTypeHelper;

public class ReaderUtil
{

	public static FileHeader parseHeader(CompactReader reader)
			throws IOException
	{
		byte[] magic = new byte[FileHeader.MAGIC.length];
		reader.readFully(magic);
		if (!Arrays.equals(magic, FileHeader.MAGIC)) {
			throw new IOException("Not a TBO file: wrong magic code");
		}

		int version = (int) reader.readVariableLengthUnsignedInteger();

		Map<String, String> tags = new TreeMap<String, String>();
		int numTags = (int) reader.readVariableLengthUnsignedInteger();
		for (int i = 0; i < numTags; i++) {
			String key = reader.readString();
			String value = reader.readString();
			tags.put(key, value);
		}

		int flags = reader.readByte();
		boolean hasMetadata = (flags & FileHeader.FLAG_HAS_METADATA) != 0;
		boolean hasBounds = (flags & FileHeader.FLAG_HAS_BOUNDS) != 0;

		OsmBounds bounds = null;
		if (hasBounds) {
			double left = Double.longBitsToDouble(reader.readLong());
			double right = Double.longBitsToDouble(reader.readLong());
			double bottom = Double.longBitsToDouble(reader.readLong());
			double top = Double.longBitsToDouble(reader.readLong());
			bounds = new Bounds(left, right, top, bottom);
		}

		return new FileHeader(version, tags, hasMetadata, bounds);
	}

	private static List<String> parsePool(CompactReader reader)
			throws IOException
	{
		List<String> pool = new ArrayList<String>();
		long size = reader.readVariableLengthUnsignedInteger();
		for (int i = 0; i < size; i++) {
			String string = reader.readString();
			pool.add(string);
		}
		return pool;
	}

	private static double fromLong(long value)
	{
		return value * .0000001;
	}

	private static List<Tag> parseTags(CompactReader reader, List<String> pool)
			throws IOException
	{
		int num = (int) reader.readVariableLengthUnsignedInteger();
		List<Tag> tags = new ArrayList<Tag>();
		for (int i = 0; i < num; i++) {
			int k = (int) reader.readVariableLengthUnsignedInteger();
			int v = (int) reader.readVariableLengthUnsignedInteger();
			String key = pool.get(k);
			String value = pool.get(v);
			Tag tag = new Tag(key, value);
			tags.add(tag);
		}
		return tags;
	}

	public static List<Node> parseNodes(CompactReader reader, FileBlock block,
			boolean hasMetadata, boolean fetchMetadata) throws IOException
	{
		List<String> poolTags = parsePool(reader);

		int n = block.getNumObjects();
		List<Node> nodes = new ArrayList<Node>(n);

		long idOffset = 0;
		long latOffset = 0;
		long lonOffset = 0;

		long[] ids = new long[n];
		double[] lats = new double[n];
		double[] lons = new double[n];

		for (int i = 0; i < n; i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			ids[i] = idOffset = id;
		}

		for (int i = 0; i < n; i++) {
			long mlat = latOffset + reader.readVariableLengthSignedInteger();
			long mlon = lonOffset + reader.readVariableLengthSignedInteger();
			lats[i] = fromLong(mlat);
			lons[i] = fromLong(mlon);
			latOffset = mlat;
			lonOffset = mlon;
		}

		for (int i = 0; i < n; i++) {
			List<Tag> tags = parseTags(reader, poolTags);
			Node node = new Node(ids[i], lons[i], lats[i]);
			nodes.add(node);
			node.setTags(tags);
		}

		if (hasMetadata && fetchMetadata) {
			parseMetadata(reader, nodes);
		}

		return nodes;
	}

	public static TLongList parseNodeIds(CompactReader reader, FileBlock block)
			throws IOException
	{
		parsePool(reader);

		TLongList ids = new TLongArrayList();

		long idOffset = 0;

		int n = block.getNumObjects();

		for (int i = 0; i < n; i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			ids.add(id);
			idOffset = id;
		}

		return ids;
	}

	public static List<Way> parseWays(CompactReader reader, FileBlock block,
			boolean hasMetadata, boolean fetchMetadata) throws IOException
	{
		List<String> poolTags = parsePool(reader);

		int n = block.getNumObjects();
		List<Way> ways = new ArrayList<Way>(n);

		long idOffset = 0;
		long nidOffset = 0;

		for (int i = 0; i < n; i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			idOffset = id;

			TLongArrayList nodeIds = new TLongArrayList();
			long numNodes = reader.readVariableLengthUnsignedInteger();
			for (int k = 0; k < numNodes; k++) {
				long nid = nidOffset + reader.readVariableLengthSignedInteger();
				nodeIds.add(nid);
				nidOffset = nid;
			}

			Way way = new Way(id, nodeIds);
			ways.add(way);
		}

		for (int i = 0; i < n; i++) {
			List<Tag> tags = parseTags(reader, poolTags);
			ways.get(i).setTags(tags);
		}

		if (hasMetadata && fetchMetadata) {
			parseMetadata(reader, ways);
		}

		return ways;
	}

	public static TLongList parseWayIds(CompactReader reader, FileBlock block)
			throws IOException
	{
		parsePool(reader);

		TLongList ids = new TLongArrayList();

		long idOffset = 0;

		for (int i = 0; i < block.getNumObjects(); i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			ids.add(id);
			idOffset = id;

			long numNodes = reader.readVariableLengthUnsignedInteger();
			for (int k = 0; k < numNodes; k++) {
				reader.readVariableLengthSignedInteger();
			}
		}

		return ids;
	}

	public static List<Relation> parseRelations(CompactReader reader,
			FileBlock block, boolean hasMetadata, boolean fetchMetadata)
			throws IOException
	{
		List<String> poolTags = parsePool(reader);
		List<String> poolMembers = parsePool(reader);

		int n = block.getNumObjects();
		List<Relation> relations = new ArrayList<Relation>(n);

		long idOffset = 0;
		long midOffset = 0;

		for (int i = 0; i < n; i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			idOffset = id;

			List<RelationMember> members = new ArrayList<RelationMember>();
			long numMembers = reader.readVariableLengthUnsignedInteger();
			for (int k = 0; k < numMembers; k++) {
				int typeByte = reader.readByte();
				long mid = midOffset + reader.readVariableLengthSignedInteger();
				midOffset = mid;
				int roleIndex = (int) reader
						.readVariableLengthUnsignedInteger();
				String role = poolMembers.get(roleIndex);
				EntityType type = EntityTypeHelper.getType(typeByte);
				members.add(new RelationMember(mid, type, role));
			}

			Relation relation = new Relation(id, members);
			relations.add(relation);
		}

		for (int i = 0; i < n; i++) {
			List<Tag> tags = parseTags(reader, poolTags);
			relations.get(i).setTags(tags);
		}

		if (hasMetadata && fetchMetadata) {
			parseMetadata(reader, relations);
		}

		return relations;
	}

	public static TLongList parseRelationIds(CompactReader reader,
			FileBlock block) throws IOException
	{
		parsePool(reader);
		parsePool(reader);

		TLongList ids = new TLongArrayList();

		long idOffset = 0;

		for (int i = 0; i < block.getNumObjects(); i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			ids.add(id);
			idOffset = id;

			long numMembers = reader.readVariableLengthUnsignedInteger();
			for (int k = 0; k < numMembers; k++) {
				reader.readByte();
				reader.readVariableLengthSignedInteger();
				reader.readVariableLengthUnsignedInteger();
			}
		}

		return ids;
	}

	private static void parseMetadata(CompactReader reader,
			List<? extends Entity> elements) throws IOException
	{
		int situationByte = reader.readByte();
		BlockMetadataInfo situation = null;
		if (situationByte == Definitions.METADATA_ALL) {
			situation = BlockMetadataInfo.ALL;
		} else if (situationByte == Definitions.METADATA_NONE) {
			situation = BlockMetadataInfo.NONE;
		} else if (situationByte == Definitions.METADATA_MIXED) {
			situation = BlockMetadataInfo.MIXED;
		}

		if (situation == null || situation == BlockMetadataInfo.NONE) {
			return;
		}

		List<String> poolUsernames = parsePool(reader);

		// number of elements in the block
		int numElements = elements.size();
		// number of meta data entries to follow
		int numMetaData;

		// Determine the number of meta data entries and which entries do have
		// data in case of MIXED
		boolean[] hasMeta = null;
		if (situation == BlockMetadataInfo.MIXED) {
			// parse flags, increment the number of entries and store flags for
			// later being able to map meta data to entities.
			numMetaData = 0;
			hasMeta = new boolean[numElements];
			for (int i = 0; i < numElements; i++) {
				int flag = reader.readByte();
				boolean thisHasMeta = flag == Definitions.METADATA_YES;
				hasMeta[i] = thisHasMeta;
				if (thisHasMeta) {
					numMetaData++;
				}
			}
		} else {
			// all entries have meta data
			numMetaData = elements.size();
		}

		// parse data entries
		int[] versions = parseDeltaInts(reader, numMetaData);
		long[] timestamps = parseDeltaLongs(reader, numMetaData);
		long[] changesets = parseDeltaLongs(reader, numMetaData);
		long[] userIds = parseDeltaLongs(reader, numMetaData);
		int[] userNameIds = parseInts(reader, numMetaData);

		// Create meta data objects and add to entity objects
		if (situation == BlockMetadataInfo.ALL) {
			// simple loop since every element has meta data
			for (int i = 0; i < numElements; i++) {
				Entity entity = elements.get(i);
				String user = poolUsernames.get(userNameIds[i]);
				OsmMetadata metadata = new Metadata(versions[i], timestamps[i],
						userIds[i], user, changesets[i]);

				entity.setMetadata(metadata);
			}
		} else {
			// a bit more complicated loop since only some elements have meta
			// data
			int i = 0;
			for (int k = 0; k < numElements; k++) {
				if (!hasMeta[k]) {
					continue;
				}
				Entity entity = elements.get(k);
				String user = poolUsernames.get(userNameIds[i]);
				OsmMetadata metadata = new Metadata(versions[i], timestamps[i],
						userIds[i], user, changesets[i]);
				i += 1;

				entity.setMetadata(metadata);
			}
		}
	}

	private static int[] parseDeltaInts(CompactReader reader, int n)
			throws IOException
	{
		int[] values = new int[n];
		long offset = 0;
		for (int i = 0; i < n; i++) {
			long delta = reader.readVariableLengthSignedInteger();
			long value = offset + delta;
			values[i] = (int) value;
			offset = value;
		}
		return values;
	}

	private static long[] parseDeltaLongs(CompactReader reader, int n)
			throws IOException
	{
		long[] values = new long[n];
		long offset = 0;
		for (int i = 0; i < n; i++) {
			long delta = reader.readVariableLengthSignedInteger();
			long value = offset + delta;
			values[i] = value;
			offset = value;
		}
		return values;
	}

	private static int[] parseInts(CompactReader reader, int n)
			throws IOException
	{
		int[] values = new int[n];
		for (int i = 0; i < n; i++) {
			long v = reader.readVariableLengthUnsignedInteger();
			values[i] = (int) v;
		}
		return values;
	}

}

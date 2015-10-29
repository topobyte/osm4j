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

import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.data.FileHeader;
import de.topobyte.osm4j.tbo.io.CompactReader;
import de.topobyte.osm4j.tbo.io.InputStreamCompactReader;
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

		int version = (int) reader.readVariableLengthSignedInteger();

		Map<String, String> tags = new TreeMap<String, String>();
		int numTags = (int) reader.readVariableLengthSignedInteger();
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
		long size = reader.readVariableLengthSignedInteger();
		for (int i = 0; i < size; i++) {
			String string = reader.readString();
			pool.add(string);
		}
		return pool;
	}

	private static double fromLong(long value)
	{
		return value * .000000001;
	}

	private static List<Tag> parseTags(CompactReader reader, List<String> pool)
			throws IOException
	{
		int num = (int) reader.readVariableLengthSignedInteger();
		List<Tag> tags = new ArrayList<Tag>();
		for (int i = 0; i < num; i++) {
			int k = (int) reader.readVariableLengthSignedInteger();
			int v = (int) reader.readVariableLengthSignedInteger();
			String key = pool.get(k);
			String value = pool.get(v);
			Tag tag = new Tag(key, value);
			tags.add(tag);
		}
		return tags;
	}

	public static List<Node> parseNodes(CompactReader reader, FileBlock block)
			throws IOException
	{
		List<String> pool = parsePool(reader);

		List<Node> nodes = new ArrayList<Node>(block.getNumObjects());

		long idOffset = 0;
		long latOffset = 0;
		long lonOffset = 0;

		for (int i = 0; i < block.getNumObjects(); i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			long mlat = latOffset + reader.readVariableLengthSignedInteger();
			long mlon = lonOffset + reader.readVariableLengthSignedInteger();
			double lat = fromLong(mlat);
			double lon = fromLong(mlon);

			idOffset = id;
			latOffset = mlat;
			lonOffset = mlon;

			// System.out.println(String.format("id: %d lon: %.10f, lat: %.10f",
			// id, lon, lat));

			List<Tag> tags = parseTags(reader, pool);
			Node node = new Node(id, lon, lat);
			nodes.add(node);
			node.setTags(tags);
		}

		return nodes;
	}

	public static List<Way> parseWays(InputStreamCompactReader reader,
			FileBlock block) throws IOException
	{
		List<String> pool = parsePool(reader);

		List<Way> ways = new ArrayList<Way>(block.getNumObjects());

		long idOffset = 0;
		long nidOffset = 0;

		for (int i = 0; i < block.getNumObjects(); i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			idOffset = id;

			TLongArrayList nodeIds = new TLongArrayList();
			long numNodes = reader.readVariableLengthSignedInteger();
			for (int k = 0; k < numNodes; k++) {
				long nid = nidOffset + reader.readVariableLengthSignedInteger();
				nodeIds.add(nid);
				nidOffset = nid;
			}

			// System.out.println(String.format("way id: %d", id));

			List<Tag> tags = parseTags(reader, pool);

			Way way = new Way(id, nodeIds);
			ways.add(way);
			way.setTags(tags);
		}

		return ways;
	}

	public static List<Relation> parseRelations(
			InputStreamCompactReader reader, FileBlock block)
			throws IOException
	{
		List<String> pool = parsePool(reader);

		List<Relation> relations = new ArrayList<Relation>(
				block.getNumObjects());

		long idOffset = 0;
		long midOffset = 0;

		for (int i = 0; i < block.getNumObjects(); i++) {
			long id = idOffset + reader.readVariableLengthSignedInteger();
			idOffset = id;

			List<RelationMember> members = new ArrayList<RelationMember>();
			long numMembers = reader.readVariableLengthSignedInteger();
			for (int k = 0; k < numMembers; k++) {
				int typeByte = reader.readByte();
				long mid = midOffset + reader.readVariableLengthSignedInteger();
				midOffset = mid;
				int roleIndex = (int) reader.readVariableLengthSignedInteger();
				String role = pool.get(roleIndex);
				EntityType type = EntityTypeHelper.getType(typeByte);
				members.add(new RelationMember(mid, type, role));
			}

			// System.out.println(String.format("relation id: %d", id));

			List<Tag> tags = parseTags(reader, pool);
			Relation relation = new Relation(id, members);
			relations.add(relation);
			relation.setTags(tags);
		}

		return relations;
	}

}

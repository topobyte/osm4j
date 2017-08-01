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

package de.topobyte.osm4j.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.testing.model.TestEntity;
import de.topobyte.osm4j.testing.model.TestMetadata;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestRelationMember;
import de.topobyte.osm4j.testing.model.TestTag;
import de.topobyte.osm4j.testing.model.TestWay;

public class EntityGenerator
{

	private int idSpan;
	private boolean generateMetadata;

	private Random random = new Random();

	private long lastNodeId = 0;
	private long lastWayId = 0;
	private long lastRelationId = 0;

	private double minLon = -180;
	private double maxLon = 180;
	private double minLat = -90;
	private double maxLat = 90;
	private int minNodes = 2;
	private int maxNodes = 50;
	private int minMembers = 2;
	private int maxMembers = 50;
	private int minLengthRoles = 3;
	private int maxLengthRoles = 12;
	private int minLengthUsernames = 5;
	private int maxLengthUsernames = 15;
	private int minLengthKeys = 3;
	private int maxLengthKeys = 12;
	private int minLengthValues = 6;
	private int maxLengthValues = 20;
	private int minTags = 0;
	private int maxTags = 10;

	public EntityGenerator(int idSpan, boolean generateMetadata)
	{
		this.idSpan = idSpan;
		this.generateMetadata = generateMetadata;
	}

	public int getIdSpan()
	{
		return idSpan;
	}

	public void setIdSpan(int idSpan)
	{
		this.idSpan = idSpan;
	}

	public boolean isGenerateMetadata()
	{
		return generateMetadata;
	}

	public void setGenerateMetadata(boolean generateMetadata)
	{
		this.generateMetadata = generateMetadata;
	}

	public TestNode generateNode()
	{
		long id = nodeId();
		double lon = lon();
		double lat = lat();
		TestNode node = new TestNode(id, lon, lat);
		generateTags(node);
		if (generateMetadata) {
			generateMetadata(node);
		}
		return node;
	}

	public TestWay generateWay()
	{
		long id = wayId();
		TLongList nodes = new TLongArrayList();
		int numNodes = minNodes + random.nextInt(maxNodes - minNodes);
		for (int i = 0; i < numNodes; i++) {
			long node = positiveLong();
			nodes.add(node);
		}
		TestWay way = new TestWay(id, nodes);
		generateTags(way);
		if (generateMetadata) {
			generateMetadata(way);
		}
		return way;
	}

	public TestRelation generateRelation()
	{
		long id = relationId();
		List<TestRelationMember> members = new ArrayList<>();
		int numMembers = minMembers + random.nextInt(maxMembers - minMembers);
		for (int i = 0; i < numMembers; i++) {
			long member = positiveLong();
			EntityType type = type();
			String role = role();
			members.add(new TestRelationMember(member, type, role));
		}
		TestRelation relation = new TestRelation(id, members);
		generateTags(relation);
		if (generateMetadata) {
			generateMetadata(relation);
		}
		return relation;
	}

	private long nodeId()
	{
		long id = lastNodeId + 1 + random.nextInt(idSpan);
		lastNodeId = id;
		return id;
	}

	private long wayId()
	{
		long id = lastWayId + 1 + random.nextInt(idSpan);
		lastWayId = id;
		return id;
	}

	private long relationId()
	{
		long id = lastRelationId + 1 + random.nextInt(idSpan);
		lastRelationId = id;
		return id;
	}

	private double lon()
	{
		double v = random.nextDouble();
		return minLon + (maxLon - minLon) * v;
	}

	private double lat()
	{
		double v = random.nextDouble();
		return minLat + (maxLat - minLat) * v;
	}

	private EntityType type()
	{
		int v = random.nextInt(3);
		switch (v) {
		default:
		case 0:
			return EntityType.Node;
		case 1:
			return EntityType.Way;
		case 2:
			return EntityType.Relation;
		}
	}

	private String role()
	{
		return string(minLengthRoles, maxLengthRoles);
	}

	private String username()
	{
		return string(minLengthUsernames, maxLengthUsernames);
	}

	private String key()
	{
		return string(minLengthKeys, maxLengthKeys);
	}

	private String value()
	{
		return string(minLengthValues, maxLengthValues);
	}

	private String string(int minLength, int maxLength)
	{
		StringBuilder builder = new StringBuilder();
		long length = minLength + random.nextInt(maxLength - minLength);
		for (int i = 0; i < length; i++) {
			char c = character();
			builder.append(c);
		}
		return builder.toString();
	}

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private char character()
	{
		return CHARS.charAt(random.nextInt(CHARS.length()));
	}

	private void generateTags(TestEntity entity)
	{
		List<TestTag> tags = new ArrayList<>();
		long num = minTags + random.nextInt(maxTags - minTags);
		for (int i = 0; i < num; i++) {
			tags.add(new TestTag(key(), value()));
		}
		entity.setTags(tags);
	}

	private void generateMetadata(TestEntity entity)
	{
		int version = random.nextInt();
		long timestamp = positiveLong() * 1000;
		long uid = positiveLong();
		String user = username();
		long changeset = positiveLong();
		TestMetadata metadata = new TestMetadata(version, timestamp, uid, user,
				changeset);
		entity.setMetadata(metadata);
	}

	private long positiveLong()
	{
		return random.nextInt(Integer.MAX_VALUE - 2) + 1;
	}

}

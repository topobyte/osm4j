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

import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.testing.model.TestNode;
import de.topobyte.osm4j.testing.model.TestRelation;
import de.topobyte.osm4j.testing.model.TestRelationMember;
import de.topobyte.osm4j.testing.model.TestWay;

public class TestDataInequality
{

	EntityGenerator entityGenerator = new EntityGenerator(10, true);
	DataSetGenerator dataSetGenerator = new DataSetGenerator(entityGenerator);

	int n = 10;

	DataSet data = dataSetGenerator.generate(n, n, n);

	/*
	 * Remove any element
	 */

	@Test
	public void testNodeRemoval()
	{
		// Remove any of the nodes
		for (int i = 0; i < data.getNodes().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getNodes().remove(i);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	@Test
	public void testWayRemoval()
	{
		// Remove any of the ways
		for (int i = 0; i < data.getWays().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getWays().remove(i);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	@Test
	public void testRelationRemoval()
	{
		// Remove any of the relations
		for (int i = 0; i < data.getRelations().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getRelations().remove(i);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	/*
	 * Remove any meta data
	 */

	@Test
	public void testNodeMetadataRemoval()
	{
		// Remove any of the nodes' meta data
		for (int i = 0; i < data.getNodes().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getNodes().get(i).setMetadata(null);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	@Test
	public void testWayMetadataRemoval()
	{
		// Remove any of the ways' meta data
		for (int i = 0; i < data.getWays().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getWays().get(i).setMetadata(null);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	@Test
	public void testRelationMetadataRemoval()
	{
		// Remove any of the relations' meta data
		for (int i = 0; i < data.getRelations().size(); i++) {
			DataSet copy = new DataSet(data);
			copy.getRelations().get(i).setMetadata(null);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	/*
	 * Node manipulation
	 */

	@Test
	public void testChangeLongitude()
	{
		// Change longitude
		for (int i = 0; i < data.getNodes().size(); i++) {
			DataSet copy = new DataSet(data);
			TestNode node = copy.getNodes().get(i);
			double lon = node.getLongitude();
			lon = lon < 0 ? lon + 1 : lon - 1;
			node.setLongitude(lon);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	@Test
	public void testChangeLatitude()
	{
		// Change latitude
		for (int i = 0; i < data.getNodes().size(); i++) {
			DataSet copy = new DataSet(data);
			TestNode node = copy.getNodes().get(i);
			double lat = node.getLatitude();
			lat = lat < 0 ? lat + 1 : lat - 1;
			node.setLatitude(lat);
			Assert.assertFalse(DataSetHelper.equals(data, copy));
		}
	}

	/*
	 * Way manipulation
	 */

	@Test
	public void testRemoveWayNodes()
	{
		for (int i = 0; i < data.getWays().size(); i++) {
			TestWay w = data.getWays().get(i);
			for (int k = 0; k < w.getNumberOfNodes(); k++) {
				DataSet copy = new DataSet(data);
				TestWay way = copy.getWays().get(i);
				way.getNodes().removeAt(k);
				Assert.assertFalse(DataSetHelper.equals(data, copy));
			}
		}
	}

	@Test
	public void testChangeWayNodes()
	{
		for (int i = 0; i < data.getWays().size(); i++) {
			TestWay w = data.getWays().get(i);
			for (int k = 0; k < w.getNumberOfNodes(); k++) {
				DataSet copy = new DataSet(data);
				TestWay way = copy.getWays().get(i);
				long ref = way.getNodes().get(k);
				way.getNodes().set(k, ref + 1);
				Assert.assertFalse(DataSetHelper.equals(data, copy));
			}
		}
	}

	/*
	 * Relation manipulation
	 */

	@Test
	public void testRemoveRelationMembers()
	{
		for (int i = 0; i < data.getRelations().size(); i++) {
			TestRelation r = data.getRelations().get(i);
			for (int k = 0; k < r.getNumberOfMembers(); k++) {
				DataSet copy = new DataSet(data);
				TestRelation relation = copy.getRelations().get(i);
				relation.getMembers().remove(k);
				Assert.assertFalse(DataSetHelper.equals(data, copy));
			}
		}
	}

	@Test
	public void testChangeRelationMemberRole()
	{
		for (int i = 0; i < data.getRelations().size(); i++) {
			TestRelation r = data.getRelations().get(i);
			for (int k = 0; k < r.getNumberOfMembers(); k++) {
				DataSet copy = new DataSet(data);
				TestRelation relation = copy.getRelations().get(i);
				TestRelationMember member = relation.getMembers().get(k);
				member.setRole(member.getRole() + "_changed");
				Assert.assertFalse(DataSetHelper.equals(data, copy));
			}
		}
	}

	@Test
	public void testChangeRelationMemberType()
	{
		for (int i = 0; i < data.getRelations().size(); i++) {
			TestRelation r = data.getRelations().get(i);
			for (int k = 0; k < r.getNumberOfMembers(); k++) {
				DataSet copy = new DataSet(data);
				TestRelation relation = copy.getRelations().get(i);
				TestRelationMember member = relation.getMembers().get(k);
				EntityType type = member.getType();
				if (type == EntityType.Node) {
					type = EntityType.Way;
				} else {
					type = EntityType.Node;
				}
				member.setType(type);
				Assert.assertFalse(DataSetHelper.equals(data, copy));
			}
		}
	}

}

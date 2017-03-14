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

package de.topobyte.osm4j.core.resolve;

import gnu.trove.TLongCollection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public interface EntityFinder
{

	public List<OsmNode> findNodes(TLongCollection ids)
			throws EntityNotFoundException;

	public List<OsmWay> findWays(TLongCollection ids)
			throws EntityNotFoundException;

	public List<OsmRelation> findRelations(TLongCollection ids)
			throws EntityNotFoundException;

	public void findWayNodes(OsmWay way, Collection<OsmNode> outNodes)
			throws EntityNotFoundException;

	public void findWayNodes(Collection<OsmWay> ways,
			Collection<OsmNode> outNodes) throws EntityNotFoundException;

	public void findMemberNodes(OsmRelation relation, Set<OsmNode> outNodes)
			throws EntityNotFoundException;

	public void findMemberNodes(Collection<OsmRelation> relations,
			Set<OsmNode> outNodes) throws EntityNotFoundException;

	public void findMemberWays(OsmRelation relation, Set<OsmWay> outWays)
			throws EntityNotFoundException;

	public void findMemberWays(Collection<OsmRelation> relations,
			Set<OsmWay> outWays) throws EntityNotFoundException;

	public void findMemberWays(OsmRelation relation, MultiSet<OsmWay> outWays)
			throws EntityNotFoundException;

	public void findMemberWays(Collection<OsmRelation> relations,
			MultiSet<OsmWay> outWays) throws EntityNotFoundException;

	public void findMemberRelations(OsmRelation relation,
			Set<OsmRelation> outRelations) throws EntityNotFoundException;

	public void findMemberRelations(Collection<OsmRelation> relations,
			Set<OsmRelation> outRelations) throws EntityNotFoundException;

	public void findMemberRelationsRecursively(OsmRelation relation,
			Set<OsmRelation> outRelations) throws EntityNotFoundException;

	public void findMemberRelationsRecursively(
			Collection<OsmRelation> relations, Set<OsmRelation> outRelations)
			throws EntityNotFoundException;

	public void findMemberNodesAndWays(OsmRelation relation,
			Set<OsmNode> outNodes, Set<OsmWay> outWays)
			throws EntityNotFoundException;

	public void findMemberNodesAndWays(Collection<OsmRelation> relations,
			Set<OsmNode> outNodes, Set<OsmWay> outWays)
			throws EntityNotFoundException;

	public void findMemberNodesAndWayNodes(OsmRelation relation,
			Set<OsmNode> nodes) throws EntityNotFoundException;

	public void findMemberNodesAndWayNodes(Collection<OsmRelation> relations,
			Set<OsmNode> nodes) throws EntityNotFoundException;

}

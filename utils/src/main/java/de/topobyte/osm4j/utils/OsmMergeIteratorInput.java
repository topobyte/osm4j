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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.merge.sorted.SortedMergeIterator;
import de.topobyte.osm4j.utils.sort.IdComparator;

public class OsmMergeIteratorInput implements OsmIteratorInput
{

	private Collection<InputStream> inputs;
	private Collection<OsmIterator> iterators;

	private Comparator<? super OsmNode> comparatorNodes;
	private Comparator<? super OsmWay> comparatorWays;
	private Comparator<? super OsmRelation> comparatorRelations;

	public OsmMergeIteratorInput(Collection<InputStream> inputs,
			Collection<OsmIterator> iterators)
	{
		this(inputs, iterators, new IdComparator());
	}

	public OsmMergeIteratorInput(Collection<InputStream> inputs,
			Collection<OsmIterator> iterators,
			Comparator<? super OsmEntity> comparator)
	{
		this(inputs, iterators, comparator, comparator, comparator);
	}

	public OsmMergeIteratorInput(Collection<InputStream> inputs,
			Collection<OsmIterator> iterators,
			Comparator<? super OsmNode> comparatorNodes,
			Comparator<? super OsmWay> comparatorWays,
			Comparator<? super OsmRelation> comparatorRelations)
	{
		this.inputs = inputs;
		this.iterators = iterators;
		this.comparatorNodes = comparatorNodes;
		this.comparatorWays = comparatorWays;
		this.comparatorRelations = comparatorRelations;
	}

	@Override
	public void close() throws IOException
	{
		for (InputStream input : inputs) {
			input.close();
		}
	}

	@Override
	public OsmIterator getIterator() throws IOException
	{
		return new SortedMergeIterator(iterators, comparatorNodes,
				comparatorWays, comparatorRelations);
	}

}

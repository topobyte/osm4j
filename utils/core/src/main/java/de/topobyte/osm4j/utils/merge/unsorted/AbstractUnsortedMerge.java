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

package de.topobyte.osm4j.utils.merge.unsorted;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.merge.AbstractMerge;

public class AbstractUnsortedMerge extends AbstractMerge
{

	public AbstractUnsortedMerge(Collection<OsmIterator> inputs)
	{
		super(inputs);
	}

	// This class is used to store the input sources in a dequeue and
	// stores the first element available on a source

	protected class Input<T extends OsmEntity>
	{

		OsmIterator iterator;
		T firstEntity;

		public Input(OsmIterator iterator)
		{
			this.iterator = iterator;
		}

	}

	protected <T extends OsmEntity> Input<T> createItem(T element,
			OsmIterator iterator)
	{
		Input<T> item = new Input<>(iterator);
		item.firstEntity = element;
		return item;
	}

	// One list for each entity type
	protected Deque<Input<OsmNode>> nodeItems = new LinkedList<>();
	protected Deque<Input<OsmWay>> wayItems = new LinkedList<>();
	protected Deque<Input<OsmRelation>> relationItems = new LinkedList<>();

}

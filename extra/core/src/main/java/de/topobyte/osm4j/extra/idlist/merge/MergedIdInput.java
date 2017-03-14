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

package de.topobyte.osm4j.extra.idlist.merge;

import java.io.EOFException;
import java.io.IOException;
import java.util.Collection;
import java.util.PriorityQueue;

import de.topobyte.osm4j.extra.idlist.IdInput;

public class MergedIdInput implements IdInput
{

	private Collection<IdInput> inputs;

	private PriorityQueue<MergeInput> queue;

	public MergedIdInput(Collection<IdInput> inputs) throws IOException
	{
		this.inputs = inputs;

		queue = new PriorityQueue<>(inputs.size(), new MergeInputComparator());

		for (IdInput input : inputs) {
			try {
				MergeInput mergeInput = new MergeInput(input);
				queue.add(mergeInput);
			} catch (EOFException e) {
				continue;
			}
		}
	}

	private long last = 0;

	@Override
	public long next() throws IOException
	{
		while (!queue.isEmpty()) {
			MergeInput input = queue.poll();
			long next = input.getNext();
			try {
				input.next();
				queue.add(input);
			} catch (EOFException e) {
				input.close();
			}
			if (last != next) {
				last = next;
				return next;
			}
		}
		throw new EOFException();
	}

	@Override
	public void close() throws IOException
	{
		for (IdInput input : inputs) {
			input.close();
		}
	}

}

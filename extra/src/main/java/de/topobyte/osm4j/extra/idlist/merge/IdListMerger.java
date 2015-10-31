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

import de.topobyte.osm4j.extra.idlist.IdListInputStream;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;

public class IdListMerger
{

	private IdListOutputStream output;
	private Collection<IdListInputStream> inputs;

	private PriorityQueue<MergeInput> queue;

	public IdListMerger(IdListOutputStream output,
			Collection<IdListInputStream> inputs)
	{
		this.output = output;
		this.inputs = inputs;
	}

	public void execute() throws IOException
	{
		init();
		run();
	}

	private void init() throws IOException
	{
		queue = new PriorityQueue<>(inputs.size(), new MergeInputComparator());

		for (IdListInputStream input : inputs) {
			try {
				MergeInput mergeInput = new MergeInput(input);
				queue.add(mergeInput);
			} catch (EOFException e) {
				continue;
			}
		}
	}

	private void run() throws IOException
	{
		long last = 0;
		while (!queue.isEmpty()) {
			MergeInput input = queue.poll();
			long next = input.getNext();
			if (last != next) {
				output.write(next);
				last = next;
			}
			try {
				input.next();
				queue.add(input);
			} catch (EOFException e) {
				input.close();
				continue;
			}
		}
	}

}

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

package de.topobyte.osm4j.extra.relations.split;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BatchBuilder<T>
{

	private Batch<T> batch;
	private List<List<T>> results = new ArrayList<>();

	public BatchBuilder(Batch<T> batch)
	{
		this.batch = batch;
	}

	public void add(T element)
	{
		if (batch.fits(element)) {
			batch.add(element);
		} else {
			List<T> elements = new ArrayList<>(batch.getElements());
			results.add(elements);
			batch.clear();
			batch.add(element);
		}
	}

	public void addAll(Collection<T> elements)
	{
		for (T element : elements) {
			add(element);
		}
	}

	public void finish()
	{
		if (!batch.getElements().isEmpty()) {
			List<T> elements = new ArrayList<>(batch.getElements());
			results.add(elements);
			batch.clear();
		}
	}

	public List<List<T>> getResults()
	{
		return results;
	}

}

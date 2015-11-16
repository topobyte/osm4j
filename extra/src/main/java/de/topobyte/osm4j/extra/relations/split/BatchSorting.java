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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;

public class BatchSorting
{

	public static List<List<IdBboxEntry>> sort(List<IdBboxEntry> bboxes,
			int maxMembers)
	{
		List<List<IdBboxEntry>> batches = new ArrayList<>();

		long size = 0;
		for (IdBboxEntry entry : bboxes) {
			size += entry.getSize();
		}
		System.out.println("num boxes: " + bboxes.size());
		System.out.println("total size: " + size);

		int maxSlice = (int) Math.ceil(Math.sqrt(size * maxMembers));
		System.out.println("max nodes per slice: " + maxSlice);

		sortX(bboxes);

		BatchBuilder<IdBboxEntry> sliceBuilder = new BatchBuilder<>(
				new IdBboxEntryBatch(maxSlice));
		sliceBuilder.addAll(bboxes);
		sliceBuilder.finish();

		List<List<IdBboxEntry>> slices = sliceBuilder.getResults();
		for (List<IdBboxEntry> slice : slices) {
			sortY(slice);
			BatchBuilder<IdBboxEntry> batchBuilder = new BatchBuilder<>(
					new IdBboxEntryBatch(maxMembers));
			batchBuilder.addAll(slice);
			batchBuilder.finish();
			batches.addAll(batchBuilder.getResults());
		}

		return batches;
	}

	private static void sortX(List<IdBboxEntry> bboxes)
	{
		Collections.sort(bboxes, new Comparator<IdBboxEntry>() {

			@Override
			public int compare(IdBboxEntry o1, IdBboxEntry o2)
			{
				Envelope e1 = o1.getEnvelope();
				Envelope e2 = o2.getEnvelope();
				double x1 = e1.getMaxX() + e1.getMinX() / 2;
				double x2 = e2.getMaxX() + e2.getMinX() / 2;
				return Double.compare(x1, x2);
			}
		});
	}

	private static void sortY(List<IdBboxEntry> bboxes)
	{
		Collections.sort(bboxes, new Comparator<IdBboxEntry>() {

			@Override
			public int compare(IdBboxEntry o1, IdBboxEntry o2)
			{
				Envelope e1 = o1.getEnvelope();
				Envelope e2 = o2.getEnvelope();
				double y1 = e1.getMaxY() + e1.getMinY() / 2;
				double y2 = e2.getMaxY() + e2.getMinY() / 2;
				return Double.compare(y1, y2);
			}
		});
	}

}

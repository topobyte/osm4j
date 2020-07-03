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

package de.topobyte.osm4j.extra.idextract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.extra.entitywriter.EntityWriter;
import de.topobyte.osm4j.extra.threading.ObjectBuffer;
import de.topobyte.osm4j.extra.threading.write.EntityWriterWriteRequest;
import de.topobyte.osm4j.extra.threading.write.WriteRequest;
import de.topobyte.osm4j.extra.threading.write.WriterRunner;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;

public class ThreadedExtractor extends AbstractExtractor
{

	private ObjectBuffer<WriteRequest> buffer = new ObjectBuffer<>(1000, 100);

	public ThreadedExtractor(EntityType type,
			List<ExtractionItem> extractionItems, OsmOutputConfig outputConfig,
			boolean lowMemory, OsmIterator iterator)
	{
		super(type, extractionItems, outputConfig, lowMemory, iterator);
	}

	@Override
	protected void output(EntityWriter writer, OsmEntity entity)
			throws IOException
	{
		buffer.write(new EntityWriterWriteRequest(writer, entity));
	}

	@Override
	public void execute() throws IOException
	{
		Runnable extractor = new Runnable() {

			@Override
			public void run()
			{
				try {
					executeExtraction();
					buffer.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		};

		Runnable writer = new WriterRunner(buffer);

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(extractor);
		tasks.add(writer);

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();

		finish();
	}

}

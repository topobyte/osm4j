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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import de.topobyte.largescalefileio.ClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.extra.entitywriter.EntityWriter;
import de.topobyte.osm4j.extra.entitywriter.EntityWriters;
import de.topobyte.osm4j.extra.idlist.IdInput;
import de.topobyte.osm4j.extra.idlist.IdListInputStream;
import de.topobyte.osm4j.extra.idlist.merge.MergedIdInput;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public abstract class AbstractExtractor implements Extractor
{

	private final EntityType type;
	private List<ExtractionItem> extractionItems;

	private PriorityQueue<Item> queue;
	private List<Item> items;

	private OsmOutputConfig outputConfig;
	private boolean lowMemory;

	private OsmIterator iterator;

	public AbstractExtractor(EntityType type,
			List<ExtractionItem> extractionItems, OsmOutputConfig outputConfig,
			boolean lowMemory, OsmIterator iterator)
	{
		this.type = type;
		this.extractionItems = extractionItems;
		this.outputConfig = outputConfig;
		this.lowMemory = lowMemory;
		this.iterator = iterator;
	}

	protected abstract void output(EntityWriter writer, OsmEntity entity)
			throws IOException;

	public void executeExtraction() throws IOException
	{
		queue = new PriorityQueue<>(extractionItems.size(),
				new ItemComparator());
		items = new ArrayList<>(extractionItems.size());

		ClosingFileInputStreamFactory factoryIn = new SimpleClosingFileInputStreamFactory();
		ClosingFileOutputStreamFactory factoryOut = new SimpleClosingFileOutputStreamFactory();

		for (ExtractionItem configItem : extractionItems) {
			// Input
			IdInput idInput;
			List<Path> pathsIds = configItem.getPathsIds();
			if (pathsIds.size() == 1) {
				File fileIds = pathsIds.get(0).toFile();
				InputStream inputIds = factoryIn.create(fileIds);
				inputIds = new BufferedInputStream(inputIds);
				idInput = new IdListInputStream(inputIds);
			} else {
				List<IdInput> idInputs = new ArrayList<>();
				for (Path path : pathsIds) {
					InputStream inputIds = factoryIn.create(path.toFile());
					inputIds = new BufferedInputStream(inputIds);
					idInputs.add(new IdListInputStream(inputIds));
				}
				idInput = new MergedIdInput(idInputs);
			}

			// Output
			File fileOutput = configItem.getPathOutput().toFile();
			OutputStream output = factoryOut.create(fileOutput);
			output = new BufferedOutputStream(output);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputConfig, lowMemory);

			EntityWriter writer = EntityWriters.create(type, osmOutput);

			// Add to priority queue
			try {
				Item item = new Item(idInput, output, osmOutput, writer);
				queue.add(item);
				items.add(item);
			} catch (EOFException e) {
				continue;
			}
		}

		NodeProgress progress = new NodeProgress();
		progress.printTimed(1000);

		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() != type) {
				break;
			}
			OsmEntity entity = container.getEntity();
			progress.increment();

			if (queue.isEmpty()) {
				break;
			}

			long id = entity.getId();

			Item input = queue.peek();
			long next = input.getNext();

			if (next > id) {
				// We don't need this element
				continue;
			} else if (next == id) {
				// We need this element
				write(queue.poll(), entity);
				// Could be that more outputs are waiting for this element
				while (!queue.isEmpty() && queue.peek().getNext() == id) {
					write(queue.poll(), entity);
				}
			} else {
				// Some element that we are waiting for is not available on the
				// input source
				skip(queue.poll());
				while (!queue.isEmpty() && queue.peek().getNext() < id) {
					skip(queue.poll());
				}
			}
		}

		progress.stop();
	}

	public void finish() throws IOException
	{
		for (Item item : items) {
			item.getOsmOutput().complete();
			item.getOutput().close();
		}
	}

	private void write(Item item, OsmEntity entity) throws IOException
	{
		output(item.getWriter(), entity);
		try {
			item.next();
			queue.add(item);
		} catch (EOFException e) {
			item.close();
		}
	}

	private void skip(Item input) throws IOException
	{
		try {
			input.next();
			queue.add(input);
		} catch (EOFException e) {
			input.close();
		}
	}

}

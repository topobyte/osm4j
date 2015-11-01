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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

// TODO: implement merging of OsmBounds

public class OsmMerge extends AbstractTaskSingleOutput
{

	private static final String OPTION_INPUT_FORMAT = "input_format";

	@Override
	protected String getHelpMessage()
	{
		return OsmMerge.class.getSimpleName() + " [options] <file> [<file>...]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmMerge task = new OsmMerge();

		task.setup(args);

		task.readMetadata = true;
		task.writeMetadata = true;

		task.init();

		try {
			task.run();
		} catch (IOException e) {
			System.out.println("error while running task");
			e.printStackTrace();
		}

		task.finish();
	}

	public OsmMerge()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT_FORMAT, true, true, "the file format of the input files");
		// @formatter:on
	}

	private FileFormat inputFormat;
	private List<String> additionalPaths = new ArrayList<String>();

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String inputFormatName = line.getOptionValue(OPTION_INPUT_FORMAT);
		inputFormat = FileFormat.parseFileFormat(inputFormatName);
		if (inputFormat == null) {
			System.out.println("invalid input format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		String[] additionalArguments = line.getArgs();
		if (additionalArguments.length < 2) {
			System.out.println("please specify at least two files as input");
			System.exit(1);
		}

		for (String arg : additionalArguments) {
			additionalPaths.add(arg);
		}
	}

	private List<InputStream> ins = new ArrayList<InputStream>();
	private List<Iterator<EntityContainer>> iterators = new ArrayList<Iterator<EntityContainer>>();

	@Override
	protected void init() throws IOException
	{
		super.init();

		for (String path : additionalPaths) {
			File file = new File(path);
			FileInputStream fis = new FileInputStream(file);
			InputStream in = new BufferedInputStream(fis);
			ins.add(in);
		}

		for (InputStream in : ins) {
			Iterator<EntityContainer> inputIterator = null;
			switch (inputFormat) {
			case XML:
				inputIterator = new OsmXmlIterator(in, readMetadata);
				break;
			case TBO:
				inputIterator = new TboIterator(in, readMetadata);
				break;
			case PBF:
				inputIterator = new PbfIterator(in, readMetadata);
				break;
			}
			iterators.add(inputIterator);
		}
	}

	private class Item
	{
		Iterator<EntityContainer> iterator;
		EntityType currentType;
		long currentId;
		OsmEntity currentEntity;
	}

	private long lastId = -1;
	private List<Item> items = new ArrayList<Item>();

	private Map<Iterator<EntityContainer>, Item> firstWays = new HashMap<Iterator<EntityContainer>, Item>();
	private Map<Iterator<EntityContainer>, Item> firstRelations = new HashMap<Iterator<EntityContainer>, Item>();

	public void run() throws IOException
	{
		prepare();
		iterate();
	}

	private void prepare() throws IOException
	{
		for (Iterator<EntityContainer> iterator : iterators) {
			if (!iterator.hasNext()) {
				continue;
			}
			EntityContainer container = iterator.next();
			Item item = createItem(container, iterator);
			switch (item.currentType) {
			case Node:
				items.add(item);
				break;
			case Way:
				firstWays.put(iterator, item);
				break;
			case Relation:
				firstRelations.put(iterator, item);
				break;
			}
		}
	}

	private void iterate() throws IOException
	{
		EntityType[] types = new EntityType[] { EntityType.Node,
				EntityType.Way, EntityType.Relation };
		for (EntityType type : types) {
			entities: while (true) {
				long currentMin = Long.MAX_VALUE;
				int take = -1;
				if (items.size() == 0) {
					break entities;
				}
				for (int i = 0; i < items.size(); i++) {
					Item item = items.get(i);
					if (item.currentId == lastId) {
						boolean advanced = advance(item);
						check(type, item, i, advanced);
						continue entities;
					}
					if (item.currentId < currentMin) {
						currentMin = item.currentId;
						take = i;
					}
				}
				Item item = items.get(take);
				switch (type) {
				case Node:
					osmOutputStream.write((OsmNode) item.currentEntity);
					break;
				case Way:
					osmOutputStream.write((OsmWay) item.currentEntity);
					break;
				case Relation:
					osmOutputStream.write((OsmRelation) item.currentEntity);
					break;
				}
				lastId = item.currentId;
				boolean advanced = advance(item);
				check(type, item, take, advanced);
			}
			lastId = -1;
			if (type == EntityType.Node) {
				type = EntityType.Way;
				for (Iterator<EntityContainer> iterator : iterators) {
					if (firstWays.containsKey(iterator)) {
						Item item = firstWays.get(iterator);
						items.add(item);
					}
				}
			} else if (type == EntityType.Way) {
				type = EntityType.Relation;
				for (Iterator<EntityContainer> iterator : iterators) {
					if (firstRelations.containsKey(iterator)) {
						Item item = firstRelations.get(iterator);
						items.add(item);
					}
				}
			}
		}
		osmOutputStream.complete();
		out.close();
	}

	private boolean advance(Item item)
	{
		if (!item.iterator.hasNext()) {
			return false;
		}
		EntityContainer container = item.iterator.next();
		item.currentEntity = container.getEntity();
		item.currentType = container.getType();
		item.currentId = item.currentEntity.getId();
		return true;
	}

	private void check(EntityType type, Item item, int i, boolean advanced)
	{
		if (!advanced) {
			items.remove(i);
			return;
		}
		if (type != item.currentType) {
			items.remove(i);
			if (item.currentType == EntityType.Way) {
				firstWays.put(item.iterator, item);
			} else if (item.currentType == EntityType.Relation) {
				firstRelations.put(item.iterator, item);
			}
		}
	}

	private Item createItem(EntityContainer container,
			Iterator<EntityContainer> iterator)
	{
		Item item = new Item();
		item.currentEntity = container.getEntity();
		item.currentType = container.getType();
		item.currentId = item.currentEntity.getId();
		item.iterator = iterator;
		return item;
	}

}

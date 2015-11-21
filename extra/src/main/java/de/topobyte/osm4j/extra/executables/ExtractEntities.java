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

package de.topobyte.osm4j.extra.executables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.extra.idextract.ExtractionItem;
import de.topobyte.osm4j.extra.idextract.ExtractionUtil;
import de.topobyte.osm4j.extra.idextract.Extractor;
import de.topobyte.osm4j.utils.AbstractExecutableSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractEntities extends AbstractExecutableSingleInputStreamOutput
{

	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FILE_NAMES_IDS = "ids";
	private static final String OPTION_FILE_NAMES_OUTPUT = "output";
	private static final String OPTION_TYPE = "type";

	@Override
	protected String getHelpMessage()
	{
		return ExtractEntities.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		ExtractEntities task = new ExtractEntities();

		task.setup(args);

		task.init();

		task.execute();
	}

	private String[] pathsData;

	private String[] fileNamesIds;
	private String fileNamesOutput;

	private EntityType type;

	public ExtractEntities()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_DIRECTORY, true, true, "directory to work on");
		OptionHelper.add(options, OPTION_FILE_NAMES_IDS, true, true, "names of the id files in the directory");
		OptionHelper.add(options, OPTION_FILE_NAMES_OUTPUT, true, true, "names of the data files to create");
		OptionHelper.add(options, OPTION_TYPE, true, true, "entity type (nodes, ways, relations)");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathsData = line.getOptionValues(OPTION_DIRECTORY);

		fileNamesIds = line.getOptionValues(OPTION_FILE_NAMES_IDS);
		fileNamesOutput = line.getOptionValue(OPTION_FILE_NAMES_OUTPUT);

		String argType = line.getOptionValue(OPTION_TYPE);
		if (argType.equals("nodes")) {
			type = EntityType.Node;
		} else if (argType.equals("ways")) {
			type = EntityType.Way;
		} else if (argType.equals("relations")) {
			type = EntityType.Relation;
		} else {
			System.out.println("Please specify a valid entity type");
			System.exit(1);
		}
	}

	private List<ExtractionItem> extractionItems = new ArrayList<>();

	@Override
	protected void init() throws IOException
	{
		super.init();

		Path[] dirsData = new Path[pathsData.length];
		for (int i = 0; i < dirsData.length; i++) {
			dirsData[i] = Paths.get(pathsData[i]);
		}

		for (Path dirData : dirsData) {
			if (!Files.isDirectory(dirData)) {
				System.out.println("Data path is not a directory: " + dirData);
				System.exit(1);
			}
		}

		for (Path dirData : dirsData) {
			List<ExtractionItem> items = ExtractionUtil.createExtractionItems(
					dirData, fileNamesIds, fileNamesOutput);
			extractionItems.addAll(items);
		}
	}

	private void execute() throws IOException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		Extractor extractor = new Extractor(type, extractionItems, outputConfig);
		OsmIterator iterator = createIterator();
		extractor.execute(iterator);
		finish();
	}

}

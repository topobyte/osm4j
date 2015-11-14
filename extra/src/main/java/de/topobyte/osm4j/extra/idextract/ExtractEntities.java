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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIteratorOutput;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class ExtractEntities extends AbstractTaskSingleInputIteratorOutput
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

	private Path[] dirsData;
	private List<Path> subdirs;

	@Override
	protected void init() throws IOException
	{
		super.init();
		dirsData = new Path[pathsData.length];
		for (int i = 0; i < dirsData.length; i++) {
			dirsData[i] = Paths.get(pathsData[i]);
		}

		for (Path dirData : dirsData) {
			if (!Files.isDirectory(dirData)) {
				System.out.println("Data path is not a directory: " + dirData);
				System.exit(1);
			}
		}

		subdirs = new ArrayList<>();
		for (Path dirData : dirsData) {
			File[] subs = dirData.toFile().listFiles();
			sub: for (File sub : subs) {
				if (!sub.isDirectory()) {
					continue;
				}
				Path subPath = sub.toPath();
				for (String fileNameIds : fileNamesIds) {
					Path ids = subPath.resolve(fileNameIds);
					if (!Files.exists(ids)) {
						continue sub;
					}
				}
				subdirs.add(subPath);
			}
		}

		for (Path subdir : subdirs) {
			List<Path> pathsIds = new ArrayList<>();
			for (String fileNameIds : fileNamesIds) {
				pathsIds.add(subdir.resolve(fileNameIds));
			}
			Path pathOutput = subdir.resolve(fileNamesOutput);
			extractionItems.add(new ExtractionItem(pathsIds, pathOutput));
		}
	}

	private void execute() throws IOException
	{
		Extractor extractor = new Extractor(type, extractionItems,
				outputFormat, pbfConfig, tboConfig, writeMetadata);
		extractor.execute(inputIterator);
		finish();
	}

}

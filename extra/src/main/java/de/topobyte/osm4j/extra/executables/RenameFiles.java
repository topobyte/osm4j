// Copyright 2016 Sebastian Kuerten
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
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.extra.Renamer;
import de.topobyte.osm4j.utils.AbstractExecutable;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class RenameFiles extends AbstractExecutable
{

	private static final String OPTION_DIRECTORY = "directory";
	private static final String OPTION_FROM = "from";
	private static final String OPTION_TO = "to";
	private static final String OPTION_DRY = "dry";

	@Override
	protected String getHelpMessage()
	{
		return RenameFiles.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException,
			OsmInputException
	{
		RenameFiles task = new RenameFiles();

		task.setup(args);

		task.execute();
	}

	public RenameFiles()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_DIRECTORY, true, true, "input file");
		OptionHelper.add(options, OPTION_FROM, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_TO, true, true, "the maximum number of nodes per file");
		OptionHelper.add(options, OPTION_DRY, false, false, "don't do any renaming, just print what would be done");
		// @formatter:on
	}

	private Path path;
	private String from, to;
	private boolean dry;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String directory = line.getOptionValue(OPTION_DIRECTORY);
		path = Paths.get(directory);
		from = line.getOptionValue(OPTION_FROM);

		to = line.getOptionValue(OPTION_TO);

		dry = line.hasOption(OPTION_DRY);
	}

	private void execute() throws IOException, OsmInputException
	{
		Renamer renamer = new Renamer(path, from, to, dry);

		renamer.execute();
	}

}

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
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractTaskSingleInputStream extends AbstractTaskInput
{

	private static final String OPTION_INPUT = "input";

	protected String pathInput = null;

	protected boolean closeInput = true;
	protected OsmStream osmStream;

	public AbstractTaskSingleInputStream()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, false, "the input file");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathInput = line.getOptionValue(OPTION_INPUT);
	}

	protected void init() throws IOException
	{
		InputStream in = null;
		if (pathInput == null) {
			closeInput = false;
			in = new BufferedInputStream(System.in);
		} else {
			closeInput = true;
			File file = new File(pathInput);
			in = StreamUtil.bufferedInputStream(file);
		}

		osmStream = new OsmStream(in, inputFormat);
	}

	protected OsmIterator createIterator() throws IOException
	{
		OsmStreamInput input = new OsmStreamInput(osmStream);
		return input.createIterator(readMetadata).getIterator();
	}

	protected OsmReader createReader() throws IOException
	{
		OsmStreamInput input = new OsmStreamInput(osmStream);
		return input.createReader(readMetadata).getReader();
	}

	protected void finish() throws IOException
	{
		if (closeInput) {
			osmStream.getInputStream().close();
		}
	}

}

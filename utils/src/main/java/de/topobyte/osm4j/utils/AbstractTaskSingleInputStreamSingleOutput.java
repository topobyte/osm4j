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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractTaskSingleInputStreamSingleOutput extends
		AbstractTaskSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT = "output";

	protected String pathOutput = null;

	protected boolean closeOutput = true;
	protected OutputStream out;

	protected OsmOutputStream osmOutputStream;

	public AbstractTaskSingleInputStreamSingleOutput()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, false, "the output file");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
	}

	@Override
	protected void init() throws IOException
	{
		super.init();

		out = null;
		if (pathOutput == null) {
			closeOutput = false;
			out = new BufferedOutputStream(System.out);
		} else {
			closeOutput = true;
			out = StreamUtil.bufferedOutputStream(pathOutput);
		}

		osmOutputStream = OsmIoUtils.setupOsmOutput(out, outputFormat,
				writeMetadata, pbfConfig, tboConfig);
	}

	@Override
	protected void finish() throws IOException
	{
		super.finish();

		out.flush();
		if (closeOutput) {
			out.close();
		}
	}

}

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractExecutableSingleOutput extends AbstractExecutable
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";

	protected FileFormat outputFormat;
	protected PbfConfig pbfConfig;
	protected TboConfig tboConfig;
	protected String pathOutput = null;

	protected boolean writeMetadata = true;

	protected boolean closeOutput = true;
	protected OutputStream out;

	protected OsmOutputStream osmOutputStream;

	public AbstractExecutableSingleOutput()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_OUTPUT, true, false, "the output file");
		OptionHelper.addL(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		PbfOptions.add(options);
		TboOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String outputFormatName = line.getOptionValue(OPTION_OUTPUT_FORMAT);
		outputFormat = FileFormat.parseFileFormat(outputFormatName);
		if (outputFormat == null) {
			System.out.println("invalid output format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pbfConfig = PbfOptions.parse(line);
		tboConfig = TboOptions.parse(line);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
	}

	protected void init() throws IOException
	{
		out = null;
		if (pathOutput == null) {
			closeOutput = false;
			out = new BufferedOutputStream(System.out);
		} else {
			closeOutput = true;
			File file = new File(pathOutput);
			FileOutputStream fos = new FileOutputStream(file);
			out = new BufferedOutputStream(fos);
		}

		switch (outputFormat) {
		case XML:
			osmOutputStream = new OsmXmlOutputStream(out, writeMetadata);
			break;
		case TBO:
			TboWriter tboWriter = new TboWriter(out, writeMetadata);
			tboWriter.setCompression(tboConfig.getCompression());
			osmOutputStream = tboWriter;
			break;
		case PBF:
			PbfWriter pbfWriter = new PbfWriter(out, writeMetadata);
			pbfWriter.setCompression(pbfConfig.getCompression());
			pbfWriter.setUseDense(pbfConfig.isUseDenseNodes());
			osmOutputStream = pbfWriter;
			break;
		}
	}

	protected void finish() throws IOException
	{
		out.flush();
		if (closeOutput) {
			out.close();
		}
	}

}

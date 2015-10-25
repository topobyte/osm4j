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

package de.topobyte.osm4j.utils.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class PbfOptions
{

	public static final String POSSIBLE_COMPRESSION_ARGUMENTS = "none, deflate";

	private static final String OPTION_PBF_COMPRESSION = "pbf_compression";
	private static final String OPTION_PBF_NONE_DENSE = "pbf_none_dense";

	public static void add(Options options)
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_PBF_COMPRESSION, true, false,
				"PBF output compression. One of " + POSSIBLE_COMPRESSION_ARGUMENTS);
		OptionHelper.add(options, OPTION_PBF_NONE_DENSE, false, false,
				"Disable dense node packing");
		// @formatter:on
	}

	public static PbfConfig parse(CommandLine line)
	{
		PbfConfig config = new PbfConfig();
		if (line.hasOption(OPTION_PBF_COMPRESSION)) {
			String compressionArg = line.getOptionValue(OPTION_PBF_COMPRESSION);
			if (compressionArg.equals("none")) {
				config.setUseCompression(false);
			} else if (compressionArg.equals("deflate")) {
				config.setUseCompression(true);
			} else {
				System.out.println("invalid compression value");
				System.out.println("please specify one of: "
						+ POSSIBLE_COMPRESSION_ARGUMENTS);
				System.exit(1);
			}
		}
		if (line.hasOption(OPTION_PBF_NONE_DENSE)) {
			config.setUseDenseNodes(false);
		}
		return config;
	}

}

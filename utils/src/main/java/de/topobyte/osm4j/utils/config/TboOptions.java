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

import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.utils.config.limit.ElementCountLimit;
import de.topobyte.osm4j.utils.config.limit.RelationMemberLimit;
import de.topobyte.osm4j.utils.config.limit.WayNodeLimit;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentHelper;
import de.topobyte.utilities.apache.commons.cli.parsing.ArgumentParseException;
import de.topobyte.utilities.apache.commons.cli.parsing.IntegerOption;

public class TboOptions
{

	public static final String POSSIBLE_COMPRESSION_ARGUMENTS = "none, deflate, lz4";

	private static final String OPTION_TBO_COMPRESSION = "tbo_compression";

	private static final String OPTION_TBO_MAX_ELEMENTS_PER_BLOCK = "tbo_max_elements_per_block";
	private static final String OPTION_TBO_MAX_NODES_PER_BLOCK = "tbo_max_nodes_per_block";
	private static final String OPTION_TBO_MAX_WAYS_PER_BLOCK = "tbo_max_ways_per_block";
	private static final String OPTION_TBO_MAX_RELATIONS_PER_BLOCK = "tbo_max_relations_per_block";
	private static final String OPTION_TBO_MAX_WAY_NODES_PER_BLOCK = "tbo_max_way_nodes_per_block";
	private static final String OPTION_TBO_MAX_RELATION_MEMBERS_PER_BLOCK = "tbo_max_relation_members_per_block";

	public static void add(Options options)
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_TBO_COMPRESSION, true, false, "TBO output compression. One of " + POSSIBLE_COMPRESSION_ARGUMENTS);
		OptionHelper.addL(options, OPTION_TBO_MAX_ELEMENTS_PER_BLOCK, true, false, "TBO: max number of elements per block");
		OptionHelper.addL(options, OPTION_TBO_MAX_NODES_PER_BLOCK, true, false, "TBO: max number of nodes per block");
		OptionHelper.addL(options, OPTION_TBO_MAX_WAYS_PER_BLOCK, true, false, "TBO: max number of ways per block");
		OptionHelper.addL(options, OPTION_TBO_MAX_RELATIONS_PER_BLOCK, true, false, "TBO: max number of relations per block");
		OptionHelper.addL(options, OPTION_TBO_MAX_WAY_NODES_PER_BLOCK, true, false, "TBO: max number of way nodes per block");
		OptionHelper.addL(options, OPTION_TBO_MAX_RELATION_MEMBERS_PER_BLOCK, true, false, "TBO: max number of relation members per block");
		// @formatter:on
	}

	public static TboConfig parse(CommandLine line)
	{
		TboConfig config = new TboConfig();
		if (line.hasOption(OPTION_TBO_COMPRESSION)) {
			String compressionArg = line.getOptionValue(OPTION_TBO_COMPRESSION);
			if (compressionArg.equals("none")) {
				config.setCompression(Compression.NONE);
			} else if (compressionArg.equals("deflate")) {
				config.setCompression(Compression.DEFLATE);
			} else if (compressionArg.equals("lz4")) {
				config.setCompression(Compression.LZ4);
			} else {
				System.out.println("invalid compression value");
				System.out.println("please specify one of: "
						+ POSSIBLE_COMPRESSION_ARGUMENTS);
				System.exit(1);
			}
		}

		try {
			IntegerOption maxElementsPerBlock = parseInteger(line,
					OPTION_TBO_MAX_ELEMENTS_PER_BLOCK);
			IntegerOption maxNodesPerBlock = parseInteger(line,
					OPTION_TBO_MAX_NODES_PER_BLOCK);
			IntegerOption maxWaysPerBlock = parseInteger(line,
					OPTION_TBO_MAX_WAYS_PER_BLOCK);
			IntegerOption maxRelationsPerBlock = parseInteger(line,
					OPTION_TBO_MAX_RELATIONS_PER_BLOCK);
			IntegerOption maxWayNodesPerBlock = parseInteger(line,
					OPTION_TBO_MAX_WAY_NODES_PER_BLOCK);
			IntegerOption maxRelationMembersPerBlock = parseInteger(line,
					OPTION_TBO_MAX_RELATION_MEMBERS_PER_BLOCK);

			if (maxElementsPerBlock.hasValue()) {
				config.setLimitNodes(new ElementCountLimit(maxElementsPerBlock
						.getValue()));
				config.setLimitWays(new ElementCountLimit(maxElementsPerBlock
						.getValue()));
				config.setLimitRelations(new ElementCountLimit(
						maxElementsPerBlock.getValue()));
			}
			if (maxNodesPerBlock.hasValue()) {
				config.setLimitNodes(new ElementCountLimit(maxNodesPerBlock
						.getValue()));
			}
			if (maxWaysPerBlock.hasValue()) {
				config.setLimitWays(new ElementCountLimit(maxWaysPerBlock
						.getValue()));
			}
			if (maxRelationsPerBlock.hasValue()) {
				config.setLimitRelations(new ElementCountLimit(
						maxRelationsPerBlock.getValue()));
			}
			if (maxWayNodesPerBlock.hasValue()) {
				config.setLimitWays(new WayNodeLimit(maxWayNodesPerBlock
						.getValue()));
			}
			if (maxRelationMembersPerBlock.hasValue()) {
				config.setLimitRelations(new RelationMemberLimit(
						maxRelationMembersPerBlock.getValue()));
			}
		} catch (ArgumentParseException e) {
			System.out
					.println("Error while parsing options: " + e.getMessage());
			System.exit(1);
		}

		return config;
	}

	private static IntegerOption parseInteger(CommandLine line, String option)
			throws ArgumentParseException
	{
		IntegerOption integer;
		try {
			integer = ArgumentHelper.getInteger(line, option);
		} catch (ArgumentParseException e) {
			throw new ArgumentParseException(String.format(
					"Unable to parse option '%s'", option));
		}
		if (integer.hasValue()) {
			if (integer.getValue() < 1) {
				throw new ArgumentParseException(String.format(
						"Option '%s' must be >= 1", option));
			}
		}
		return integer;
	}

}

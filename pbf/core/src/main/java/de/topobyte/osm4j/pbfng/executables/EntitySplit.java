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

package de.topobyte.osm4j.pbfng.executables;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import crosby.binary.Fileformat;
import crosby.binary.Fileformat.Blob;
import crosby.binary.Osmformat;
import crosby.binary.Osmformat.PrimitiveBlock;
import crosby.binary.Osmformat.PrimitiveBlock.Builder;
import crosby.binary.Osmformat.PrimitiveGroup;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbfng.Compression;
import de.topobyte.osm4j.pbfng.Constants;
import de.topobyte.osm4j.pbfng.seq.BlockWriter;
import de.topobyte.osm4j.pbfng.util.BlockHeader;
import de.topobyte.osm4j.pbfng.util.PbfMeta;
import de.topobyte.osm4j.pbfng.util.PbfUtil;
import de.topobyte.osm4j.pbfng.util.copy.EntityGroups;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class EntitySplit
{

	final static Logger logger = LoggerFactory.getLogger(EntitySplit.class);

	private static String HELP_MESSAGE = EntitySplit.class.getSimpleName()
			+ " [options]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT_NODES = "output_nodes";
	private static final String OPTION_OUTPUT_WAYS = "output_ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output_relations";

	public static void main(String[] args) throws IOException
	{
		// @formatter:off
		Options options = new Options();
		OptionHelper.add(options, OPTION_INPUT, true, false, "input file");
		OptionHelper.add(options, OPTION_OUTPUT_NODES, true, false, "the file to write nodes to");
		OptionHelper.add(options, OPTION_OUTPUT_WAYS, true, false, "the file to write ways to");
		OptionHelper.add(options, OPTION_OUTPUT_RELATIONS, true, false, "the file to write relations to");
		// @formatter:on

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			System.out.println("unable to parse command line: "
					+ e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		if (line == null) {
			return;
		}

		InputStream in = null;
		if (line.hasOption(OPTION_INPUT)) {
			String inputPath = line.getOptionValue(OPTION_INPUT);
			File file = new File(inputPath);
			FileInputStream fis = new FileInputStream(file);
			in = new BufferedInputStream(fis);
		} else {
			in = new BufferedInputStream(System.in);
		}

		OutputStream outNodes = null, outWays = null, outRelations = null;
		if (line.hasOption(OPTION_OUTPUT_NODES)) {
			String path = line.getOptionValue(OPTION_OUTPUT_NODES);
			FileOutputStream fos = new FileOutputStream(path);
			outNodes = new BufferedOutputStream(fos);
		}
		if (line.hasOption(OPTION_OUTPUT_WAYS)) {
			String path = line.getOptionValue(OPTION_OUTPUT_WAYS);
			FileOutputStream fos = new FileOutputStream(path);
			outWays = new BufferedOutputStream(fos);
		}
		if (line.hasOption(OPTION_OUTPUT_RELATIONS)) {
			String path = line.getOptionValue(OPTION_OUTPUT_RELATIONS);
			FileOutputStream fos = new FileOutputStream(path);
			outRelations = new BufferedOutputStream(fos);
		}

		if (outNodes == null && outWays == null && outRelations == null) {
			System.out
					.println("You should specify an output for at least one entity");
			System.exit(1);
		}

		EntitySplit task = new EntitySplit(in, outNodes, outWays, outRelations);
		task.execute();
	}

	private final OutputStream outNodes, outWays, outRelations;

	private boolean copyNodes;
	private boolean copyWays;
	private boolean copyRelations;

	private DataInputStream input;
	private BlockWriter blockWriterNodes = null;
	private BlockWriter blockWriterWays = null;
	private BlockWriter blockWriterRelations = null;

	public EntitySplit(InputStream in, OutputStream outNodes,
			OutputStream outWays, OutputStream outRelations)
	{
		this.outNodes = outNodes;
		this.outWays = outWays;
		this.outRelations = outRelations;

		input = new DataInputStream(in);

		copyNodes = outNodes != null;
		copyWays = outWays != null;
		copyRelations = outRelations != null;

		if (copyNodes) {
			blockWriterNodes = new BlockWriter(outNodes);
		}
		if (copyWays) {
			blockWriterWays = new BlockWriter(outWays);
		}
		if (copyRelations) {
			blockWriterRelations = new BlockWriter(outRelations);
		}
	}

	public void execute() throws IOException
	{
		while (true) {
			try {
				BlockHeader header = PbfUtil.parseHeader(input);

				Fileformat.Blob blob = PbfUtil.parseBlock(input,
						header.getDataLength());

				String type = header.getType();

				if (type.equals(Constants.BLOCK_TYPE_DATA)) {
					data(blob);
				} else if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
					if (copyNodes) {
						blockWriterNodes.write(header.getType(), null, blob);
					}
					if (copyWays) {
						blockWriterWays.write(header.getType(), null, blob);
					}
					if (copyRelations) {
						blockWriterRelations
								.write(header.getType(), null, blob);
					}
				}

			} catch (EOFException eof) {
				break;
			}
		}

		if (copyNodes) {
			outNodes.close();
		}
		if (copyWays) {
			outWays.close();
		}
		if (copyRelations) {
			outRelations.close();
		}

	}

	private void data(Blob blob) throws IOException
	{
		ByteString blockData = PbfUtil.getBlockData(blob);
		Osmformat.PrimitiveBlock primBlock = Osmformat.PrimitiveBlock
				.parseFrom(blockData);

		if (!PbfMeta.hasMixedContent(primBlock)) {
			// If the block does not contain multiple entity types, we can copy
			// the blob without have to recreate the message.
			EntityType type = PbfMeta.getContentTypes(primBlock).iterator()
					.next();
			if (type == EntityType.Node && copyNodes) {
				blockWriterNodes.write(Constants.BLOCK_TYPE_DATA, null, blob);
			} else if (type == EntityType.Way && copyWays) {
				blockWriterWays.write(Constants.BLOCK_TYPE_DATA, null, blob);
			} else if (type == EntityType.Relation && copyRelations) {
				blockWriterRelations.write(Constants.BLOCK_TYPE_DATA, null,
						blob);
			}
		} else {
			// Multiple entity types in the block. Extract types and write to
			// appropriate output.
			EntityGroups groups = EntityGroups.splitEntities(primBlock);

			Compression compression = Compression.DEFLATE;

			if (copyNodes && groups.getNodeGroups().size() > 0) {
				copy(blockWriterNodes, groups.getNodeGroups(), primBlock,
						compression);
			}

			if (copyWays && groups.getWayGroups().size() > 0) {
				copy(blockWriterWays, groups.getWayGroups(), primBlock,
						compression);
			}

			if (copyRelations && groups.getRelationGroups().size() > 0) {
				copy(blockWriterRelations, groups.getRelationGroups(),
						primBlock, compression);
			}
		}

	}

	private void copy(BlockWriter blockWriter, List<PrimitiveGroup> gs,
			Osmformat.PrimitiveBlock primBlock, Compression compression)
			throws IOException
	{
		Osmformat.PrimitiveBlock.Builder builder = Osmformat.PrimitiveBlock
				.newBuilder();
		for (Osmformat.PrimitiveGroup group : gs) {
			builder.addPrimitivegroup(group);
		}
		copyExtraData(builder, primBlock);
		Osmformat.PrimitiveBlock block = builder.build();
		blockWriter.write(Constants.BLOCK_TYPE_DATA, null, compression,
				block.toByteString());
	}

	private void copyExtraData(Builder builder, PrimitiveBlock primBlock)
	{
		builder.setGranularity(primBlock.getGranularity());
		builder.setDateGranularity(primBlock.getDateGranularity());
		builder.setStringtable(primBlock.getStringtable());
	}

}

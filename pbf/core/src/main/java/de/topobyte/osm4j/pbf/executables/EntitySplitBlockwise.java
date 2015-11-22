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

package de.topobyte.osm4j.pbf.executables;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.PrimitiveBlock;
import crosby.binary.Osmformat.PrimitiveBlock.Builder;
import crosby.binary.Osmformat.PrimitiveGroup;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbf.Compression;
import de.topobyte.osm4j.pbf.Constants;
import de.topobyte.osm4j.pbf.raf.FileStructure;
import de.topobyte.osm4j.pbf.raf.FileStructureAnalyzer;
import de.topobyte.osm4j.pbf.raf.Interval;
import de.topobyte.osm4j.pbf.raf.PbfFile;
import de.topobyte.osm4j.pbf.seq.BlockWriter;
import de.topobyte.osm4j.pbf.util.BlockData;
import de.topobyte.osm4j.pbf.util.PbfUtil;
import de.topobyte.osm4j.pbf.util.copy.EntityGroups;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class EntitySplitBlockwise
{

	final static Logger logger = LoggerFactory
			.getLogger(EntitySplitBlockwise.class);

	private static String HELP_MESSAGE = EntitySplitBlockwise.class
			.getSimpleName() + " [options]";

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT_NODES = "output_nodes";
	private static final String OPTION_OUTPUT_WAYS = "output_ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output_relations";

	public static void main(String[] args) throws IOException
	{
		// @formatter:off
		Options options = new Options();
		OptionHelper.add(options, OPTION_INPUT, true, true, "input file");
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

		String inputPath = line.getOptionValue(OPTION_INPUT);
		File file = new File(inputPath);
		PbfFile pbfFile = new PbfFile(file);

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

		EntitySplitBlockwise task = new EntitySplitBlockwise(pbfFile, outNodes,
				outWays, outRelations);
		task.execute();
	}

	private final OutputStream outNodes, outWays, outRelations;

	private boolean copyNodes;
	private boolean copyWays;
	private boolean copyRelations;

	private PbfFile pbfFile;
	private BlockWriter blockWriterNodes = null;
	private BlockWriter blockWriterWays = null;
	private BlockWriter blockWriterRelations = null;

	public EntitySplitBlockwise(PbfFile pbfFile, OutputStream outNodes,
			OutputStream outWays, OutputStream outRelations)
	{
		this.pbfFile = pbfFile;
		this.outNodes = outNodes;
		this.outWays = outWays;
		this.outRelations = outRelations;

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
		FileStructure structure = FileStructureAnalyzer.analyze(pbfFile);

		Interval nodes = structure.getBlocksNodes();
		Interval ways = structure.getBlocksWays();
		Interval relations = structure.getBlocksRelations();

		print("nodes", nodes);
		print("ways", ways);
		print("relations", relations);

		// Flag whether the last node block contains also ways or relations
		boolean lastNodeBlockMixed = false;
		// Flag whether the last way block contains also relations
		boolean lastWayBlockMixed = false;
		// Flag whether the first way block contains also nodes
		boolean firstWayBlockMixed = false;
		// Flag whether the first relation block contains also nodes or ways
		boolean firstRelationBlockMixed = false;

		if (nodes != null && ways != null) {
			lastNodeBlockMixed |= nodes.getEnd() == ways.getStart();
			firstWayBlockMixed |= nodes.getEnd() == ways.getStart();
		}
		if (nodes != null && relations != null) {
			lastNodeBlockMixed |= nodes.getEnd() == relations.getStart();
			firstRelationBlockMixed |= nodes.getEnd() == relations.getStart();
		}
		if (ways != null && relations != null) {
			lastWayBlockMixed |= ways.getEnd() == relations.getStart();
			firstRelationBlockMixed |= ways.getEnd() == relations.getStart();
		}

		System.out.println("Last node block mixed? " + lastNodeBlockMixed);
		System.out.println("First way block mixed? " + firstWayBlockMixed);
		System.out.println("Last way block mixed? " + lastWayBlockMixed);
		System.out.println("First relation block mixed? "
				+ firstRelationBlockMixed);

		if (copyNodes) {
			writeHeader(blockWriterNodes);
			copyNodes(nodes, lastNodeBlockMixed);
			outNodes.close();
		}

		if (copyWays) {
			writeHeader(blockWriterWays);
			copyWays(ways, firstWayBlockMixed, lastWayBlockMixed);
			outWays.close();
		}
		if (copyRelations) {
			writeHeader(blockWriterRelations);
			copyRelations(relations, firstRelationBlockMixed);
			outRelations.close();
		}

	}

	private void print(String type, Interval blocks)
	{
		System.out.println(String.format("%s: %d - %d", type,
				blocks.getStart(), blocks.getEnd()));
	}

	private void writeHeader(BlockWriter blockWriter) throws IOException
	{
		if (!pbfFile.hasHeader()) {
			return;
		}

		HeaderBlock header = pbfFile.getHeaderBlock();
		ByteString headerData = header.toByteString();
		blockWriter.write(Constants.BLOCK_TYPE_HEADER, null,
				Compression.DEFLATE, headerData);
	}

	private void copyNodes(Interval nodes, boolean lastNodeBlockDirty)
			throws IOException
	{
		int lastNodeOnlyBlock = nodes.getEnd();
		if (lastNodeBlockDirty) {
			lastNodeOnlyBlock -= 1;
		}

		// First copy all blocks that contain only nodes block-wise
		for (int i = nodes.getStart(); i <= lastNodeOnlyBlock; i++) {
			Fileformat.Blob blob = pbfFile.getDataBlob(i);
			blockWriterNodes.write(Constants.BLOCK_TYPE_DATA, null, blob);
		}

		// If the last block contains data other than nodes, continue
		if (!lastNodeBlockDirty) {
			return;
		}

		// Copy the remaining nodes group-wise
		copyPartial(nodes.getEnd(), EntityType.Node, blockWriterNodes);
	}

	private void copyWays(Interval ways, boolean firstWayBlockMixed,
			boolean lastWayBlockMixed) throws IOException
	{
		int firstWayOnlyBlock = ways.getStart();
		if (firstWayBlockMixed) {
			firstWayOnlyBlock += 1;
		}
		int lastWayOnlyBlock = ways.getEnd();
		if (lastWayBlockMixed) {
			lastWayOnlyBlock -= 1;
		}

		if (firstWayBlockMixed) {
			// First copy all ways from the first partial block
			copyPartial(ways.getStart(), EntityType.Way, blockWriterWays);
		}

		// Then copy all blocks that contain only ways block-wise
		for (int i = firstWayOnlyBlock; i <= lastWayOnlyBlock; i++) {
			Fileformat.Blob blob = pbfFile.getDataBlob(i);
			blockWriterWays.write(Constants.BLOCK_TYPE_DATA, null, blob);
		}

		// If the last block contains data other than ways, continue
		if (!lastWayBlockMixed) {
			return;
		}

		// But not if we copied the partial block in the beginning already
		if (ways.getStart() == ways.getEnd()) {
			return;
		}

		// Copy the remaining ways group-wise
		copyPartial(ways.getEnd(), EntityType.Way, blockWriterWays);
	}

	private void copyRelations(Interval relations,
			boolean firstRelationBlockMixed) throws IOException
	{
		int firstRelationOnlyBlock = relations.getStart();
		if (firstRelationBlockMixed) {
			firstRelationOnlyBlock += 1;
		}

		if (firstRelationBlockMixed) {
			// First copy all relations from the first partial block
			copyPartial(relations.getStart(), EntityType.Relation,
					blockWriterRelations);
		}

		// Then copy all blocks that contain only relations block-wise
		for (int i = firstRelationOnlyBlock; i <= relations.getEnd(); i++) {
			Fileformat.Blob blob = pbfFile.getDataBlob(i);
			blockWriterRelations.write(Constants.BLOCK_TYPE_DATA, null, blob);
		}
	}

	private void copyPartial(int blockIndex, EntityType type,
			BlockWriter blockWriter) throws IOException
	{
		Blob blob = pbfFile.getDataBlob(blockIndex);
		BlockData blockData = PbfUtil.getBlockData(blob);
		PrimitiveBlock block = PrimitiveBlock
				.parseFrom(blockData.getBlobData());
		EntityGroups groups = EntityGroups.splitEntities(block);

		copy(blockWriter, groups.getGroups(type), block,
				blockData.getCompression());
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

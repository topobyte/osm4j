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
//
//
// This files is based on a file from Osmosis. The original file contained this
// copyright notice:
//
// This software is released into the Public Domain. See copying.txt for details.
//
//
// And the mentioned copying.txt states:
//
// Osmosis is placed into the public domain and where this is not legally
// possible everybody is granted a perpetual, irrevocable license to use
// this work for any purpose whatsoever.
//
// DISCLAIMERS
// By making Osmosis publicly available, it is hoped that users will find the
// software useful. However:
//   * Osmosis comes without any warranty, to the extent permitted by
//     applicable law.
//   * Unless required by applicable law, no liability will be accepted by
// the authors and distributors of this software for any damages caused
// as a result of its use.

package de.topobyte.osm4j.pbf;

import java.io.IOException;

import crosby.binary.BinarySerializer;
import crosby.binary.Osmformat;
import crosby.binary.file.BlockOutputStream;
import crosby.binary.file.FileBlock;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

/**
 * Receives data from the Osmosis pipeline and stores it in the PBF format.
 */
public class PbfSerializer extends BinarySerializer implements OsmOutputStream
{

	private static String VERSION = "osm4j-pbf-0.0.1";

	protected boolean writeMetadata = true;

	/**
	 * Additional configuration flag for whether to serialize into
	 * DenseNodes/DenseInfo?
	 */
	protected boolean useDense = true;

	/** Has the header been written yet? */
	protected boolean headerWritten = false;

	/**
	 * Construct a serializer that writes to the target BlockOutputStream.
	 * 
	 * @param output
	 *            The PBF block stream to send serialized data.
	 */
	public PbfSerializer(BlockOutputStream output, boolean writeMetadata)
	{
		super(output);
		this.writeMetadata = writeMetadata;
	}

	/**
	 * Change the flag of whether to use the dense format.
	 * 
	 * @param useDense
	 *            The new use dense value.
	 */
	public void setUseDense(boolean useDense)
	{
		this.useDense = useDense;
	}

	/* One list for each type */
	private WayGroup ways;
	private NodeGroup nodes;
	private RelationGroup relations;

	public void process(OsmBounds bounds)
	{
		Osmformat.HeaderBlock.Builder headerblock = Osmformat.HeaderBlock
				.newBuilder();

		if (bounds != null) {
			Osmformat.HeaderBBox.Builder bbox = Osmformat.HeaderBBox
					.newBuilder();
			bbox.setLeft(mapRawDegrees(bounds.getLeft()));
			bbox.setBottom(mapRawDegrees(bounds.getBottom()));
			bbox.setRight(mapRawDegrees(bounds.getRight()));
			bbox.setTop(mapRawDegrees(bounds.getTop()));
			headerblock.setBbox(bbox);
		}

		finishHeader(headerblock);
	}

	public void process(OsmNode node)
	{
		if (nodes == null) {
			// Need to switch types.
			switchTypes();
			nodes = new NodeGroup(useDense, writeMetadata);
		}
		nodes.add(node);
		checkLimit();
	}

	public void process(OsmWay way)
	{
		if (ways == null) {
			switchTypes();
			ways = new WayGroup(writeMetadata);
		}
		ways.add(way);
		checkLimit();
	}

	public void process(OsmRelation relation)
	{
		if (relations == null) {
			switchTypes();
			relations = new RelationGroup(writeMetadata);
		}
		relations.add(relation);
		checkLimit();
	}

	@Override
	public void complete()
	{
		try {
			switchTypes();
			processBatch();
			flush();
		} catch (IOException e) {
			throw new RuntimeException("Unable to complete the PBF file.", e);
		}
	}

	public void release()
	{
		try {
			close();
		} catch (IOException e) {
			System.out
					.println("Unable to release PBF file resources during release.");
		}
	}

	/*
	 * internal methods
	 */

	/**
	 * Check if we've reached the batch size limit and process the batch if we
	 * have.
	 */
	private void checkLimit()
	{
		total_entities++;
		if (++batch_size < batch_limit) {
			return;
		}
		switchTypes();
		processBatch();
	}

	/**
	 * At the end of this function, all of the lists of unprocessed 'things'
	 * must be null
	 */
	private void switchTypes()
	{
		if (nodes != null) {
			groups.add(nodes);
			nodes = null;
		} else if (ways != null) {
			groups.add(ways);
			ways = null;
		} else if (relations != null) {
			groups.add(relations);
			relations = null;
		} else {
			return; // No data. Is this an empty file?
		}
	}

	/**
	 * Write the header fields that are always needed.
	 * 
	 * @param headerblock
	 *            Incomplete builder to complete and write.
	 * */
	public void finishHeader(Osmformat.HeaderBlock.Builder headerblock)
	{
		headerblock.setWritingprogram(VERSION);
		headerblock.addRequiredFeatures("OsmSchema-V0.6");
		if (useDense) {
			headerblock.addRequiredFeatures("DenseNodes");
		}
		Osmformat.HeaderBlock message = headerblock.build();
		try {
			output.write(FileBlock.newInstance("OSMHeader",
					message.toByteString(), null));
		} catch (IOException e) {
			throw new RuntimeException("Unable to write OSM header.", e);
		}
		headerWritten = true;
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		process(bounds);
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		process(node);
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		process(way);
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		process(relation);
	}

}

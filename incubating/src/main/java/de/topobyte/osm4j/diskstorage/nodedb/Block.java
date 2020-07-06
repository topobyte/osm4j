// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.nodedb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.topobyte.osm4j.diskstorage.HighLevelInputStream;
import de.topobyte.osm4j.diskstorage.HighLevelOutputStream;

/**
 * A block in the random access file that stores a number of nodes
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Block
{

	/**
	 * The number of bytes per block
	 */
	public static final int BLOCKBYTES = 4096;
	/**
	 * The number of nodes stored in a block
	 */
	public static final int BLOCKSIZE = (BLOCKBYTES - 2) / DbNode.NODESIZE;
	/**
	 * The number of bytes to pad a block with
	 */
	public static final int FILLBYTES = BLOCKBYTES - DbNode.NODESIZE * BLOCKSIZE
			- 2;

	private List<DbNode> nodes = new ArrayList<>();

	/**
	 * Add a node to this block
	 * 
	 * @param node
	 *            the node to add
	 */
	public void add(DbNode node)
	{
		getNodes().add(node);
	}

	/**
	 * Write this block to a RandomAccessFile.
	 * 
	 * @param raf
	 *            the file to write this block to.
	 * @throws IOException
	 *             if writing fails.
	 */
	public void write(RandomAccessFile raf) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BLOCKBYTES);
		HighLevelOutputStream hlos = new HighLevelOutputStream(baos);

		hlos.writeShort(getNodes().size());
		for (DbNode node : getNodes()) {
			node.write(hlos);
		}
		int left = BLOCKSIZE - getNodes().size();
		for (int i = 0; i < left; i++) {
			for (int k = 0; k < DbNode.NODESIZE; k++) {
				hlos.writeByte(0);
			}
		}
		for (int i = 0; i < FILLBYTES; i++) {
			hlos.writeByte(0);
		}

		byte[] bytes = baos.toByteArray();
		raf.write(bytes);
	}

	/**
	 * Read a block from an underlying RandomAccessFile.
	 * 
	 * @param raf
	 *            the file to read from.
	 * @param pos
	 *            the file offset to use.
	 * @return the read block.
	 * @throws IOException
	 *             if reading fails.
	 */
	public static Block read(RandomAccessFile raf, long pos) throws IOException
	{
		raf.seek(pos);
		byte[] bytes = new byte[BLOCKBYTES];
		raf.read(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		HighLevelInputStream hlis = new HighLevelInputStream(bais);

		int size = hlis.readShort();
		Block block = new Block();
		for (int i = 0; i < size; i++) {
			DbNode node = DbNode.read(hlis);
			block.getNodes().add(node);
		}
		return block;
	}

	/**
	 * Within this block, find the node with the given id.
	 * 
	 * @param id
	 *            the node's id to look for.
	 * @return the found node or null if it cannot be found.
	 */
	public DbNode find(long id)
	{
		int i = Collections.binarySearch(getNodes(), new DbNode(id, 0, 0),
				new Comparator<DbNode>() {

					@Override
					public int compare(DbNode a, DbNode b)
					{
						if (a.getId() == b.getId()) {
							return 0;
						} else if (a.getId() > b.getId()) {
							return 1;
						} else {
							return -1;
						}
					}
				});
		if (i >= 0) {
			return getNodes().get(i);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return String.format("block. nodes: " + getNodes().size());
	}

	/**
	 * Getter for nodes.
	 * 
	 * @return the list of nodes in this block.
	 */
	public List<DbNode> getNodes()
	{
		return nodes;
	}

}
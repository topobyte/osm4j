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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.diskstorage.BlockProvider;
import de.topobyte.osm4j.diskstorage.Cache;
import de.topobyte.osm4j.diskstorage.nodedb.osmmodel.NodeImpl;

/**
 * A node database consists of a RandomAccessFile to store blocks of nodes and
 * an instance of Index to provide fast access to the position of blocks that
 * contain a node that shall be retrieved. Furthermore it uses a cache for
 * blocks to improve performance.
 * 
 * This database is intended for single initialization with data. Updates are
 * not possible. It has been implemented with highest read-performance possible
 * in mind.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class NodeDB implements BlockProvider<Block>, OsmEntityProvider
{

	static final Logger logger = LoggerFactory.getLogger(NodeDB.class);

	private Index index = new Index();
	private RandomAccessFile raf;
	private Cache<Block> cache = new Cache<>(this);

	Path fileIndex;

	Block currentBlock = new Block();

	/**
	 * Create a node database that uses two given files as index and data files.
	 * 
	 * @param fileDB
	 *            the database file.
	 * @param fileIndex
	 *            the index file.
	 * @throws FileNotFoundException
	 *             if one of the files cannot be found.
	 */
	public NodeDB(Path fileDB, Path fileIndex) throws FileNotFoundException
	{
		this.fileIndex = fileIndex;
		try {
			ObjectInputStream ois = new ObjectInputStream(
					Files.newInputStream(fileIndex));
			logger.debug("reading index");
			index = (Index) ois.readObject();
			ois.close();
		} catch (Exception e) {
			logger.debug("unable to read index, creating new one");
			index = new Index();
		}
		logger.debug("opening database file");
		raf = new RandomAccessFile(fileDB.toFile(), "rw");
	}

	// counter for added nodes.
	private int nodesAdded = 0;

	/**
	 * Add a node to the database.
	 * 
	 * @param node
	 *            the node to add.
	 * @throws IOException
	 *             if an IO error occurs.
	 */
	public void addNode(DbNode node) throws IOException
	{
		nodesAdded += 1;
		currentBlock.add(node);
		if (currentBlock.getNodes().size() == Block.BLOCKSIZE) {
			writeCurrentBlock();
			currentBlock = new Block();
		}
	}

	private void writeCurrentBlock() throws IOException
	{
		currentBlock.write(raf);
		int size = currentBlock.getNodes().size();
		DbNode min = currentBlock.getNodes().get(0);
		DbNode max = currentBlock.getNodes().get(size - 1);
		getIndex().addEntry(min.getId(), max.getId(),
				getIndex().getEntries().size() * (long) Block.BLOCKBYTES);
	}

	@Override
	public Block getBlock(long pos) throws IOException
	{
		return Block.read(raf, pos);
	}

	/**
	 * Close the database after writing.
	 * 
	 * @throws IOException
	 *             if an error occurs.
	 */
	public void close() throws IOException
	{
		logger.debug("closing database");
		if (nodesAdded > 0) {
			if (currentBlock.getNodes().size() > 0) {
				writeCurrentBlock();
			}
		}
		raf.close();
		if (nodesAdded > 0) {
			logger.debug("writing index");
			logger.debug("number of nodes: " + nodesAdded);
			logger.debug("number of blocks: " + getIndex().getEntries().size());
			ObjectOutputStream oos = new ObjectOutputStream(
					Files.newOutputStream(fileIndex));
			oos.writeObject(getIndex());
			oos.close();
		}
	}

	/**
	 * Getter for the index of this database.
	 * 
	 * @return the index.
	 */
	public Index getIndex()
	{
		return index;
	}

	public boolean contains(long id)
	{
		Entry found = index.find(id);
		return found != null;
	}

	/**
	 * Retrieve from this database the node with this id.
	 * 
	 * @param id
	 *            the id to look for.
	 * @return the node found or null.
	 * @throws IOException
	 *             on IO failure.
	 */
	public DbNode find(long id) throws IOException
	{
		Entry find = index.find(id);
		if (find == null) {
			return null;
		}
		// System.out.println("pos: " + find.getPosition());
		Block block = cache.getBlock(find.getPosition());
		// System.out.println(block);
		DbNode node = block.find(id);
		if (node == null) {
			logger.debug("node not found");
			return null;
		}
		// System.out.println(node.getId() + ": " + node.getLon() + "," +
		// node.getLat());
		return node;
	}

	@Override
	public OsmNode getNode(long id) throws EntityNotFoundException
	{
		try {
			DbNode dbNode = find(id);
			if (dbNode == null) {
				throw new EntityNotFoundException("node not found in database");
			}
			return new NodeImpl(dbNode);
		} catch (IOException e) {
			throw new EntityNotFoundException(e);
		}
	}

	@Override
	public OsmWay getWay(long arg0) throws EntityNotFoundException
	{
		throw new EntityNotFoundException("NodeDB doesn't support ways");
	}

	@Override
	public OsmRelation getRelation(long arg0) throws EntityNotFoundException
	{
		throw new EntityNotFoundException("NodeDB doesn't support relations");
	}

}

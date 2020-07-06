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

package de.topobyte.osm4j.diskstorage.vardb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.diskstorage.BlockProvider;
import de.topobyte.osm4j.diskstorage.Cache;

/**
 * @param <T>
 *            the type of records to store.
 * 
 *            A read-optimized database of entries of variable size.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class VarDB<T extends Record>
{

	final static Logger logger = LoggerFactory.getLogger(VarDB.class);

	private Path fileIndex;
	private Index index;
	private RandomAccessFile raf;

	private BlockProvider<Block> blockProvider;
	private BlockProvider<Block> blockCache;

	private Block currentBlock = new Block();

	private T instance;

	/**
	 * Create a node database that uses two given files as index and data files.
	 * 
	 * @param fileDB
	 *            the database file.
	 * @param fileIndex
	 *            the index file.
	 * @param instance
	 *            an instance to of T to be able to perform typed factory
	 *            methods.
	 * @throws FileNotFoundException
	 *             if one of the files cannot be found.
	 */
	public VarDB(Path fileDB, Path fileIndex, T instance)
			throws FileNotFoundException
	{
		this.instance = instance;
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
		blockProvider = new RafBlockProvider(raf);
		blockCache = new Cache<>(blockProvider, 1000);
	}

	private int recordsAdded = 0;

	/**
	 * Add this record to the database.
	 * 
	 * @param record
	 *            the record to add.
	 */
	public void addRecord(Record record)
	{
		recordsAdded += 1;
		logger.debug(
				"block bytes remaining: " + currentBlock.getRealCapacity());
		logger.debug("record size: " + record.getNumberOfBytes());
		List<RecordPart> parts = record.toRecordParts(
				currentBlock.getRealCapacity(),
				Block.BLOCKBYTES_USER - Block.MGMT_BYTES);

		logger.debug("number of parts: " + parts.size());

		for (int i = 0; i < parts.size(); i++) {
			RecordPart part = parts.get(i);
			if (i >= 1) {
				try {
					writeCurrentBlock();
				} catch (IOException e) {
					//
				}
				currentBlock = new Block();
			}
			currentBlock.add(part);
			// logger.debug("block bytes remaining: " +
			// currentBlock.getCapacity());
		}
		if (currentBlock.getRealCapacity() <= 0) {
			try {
				writeCurrentBlock();
			} catch (IOException e) {
				//
			}
			currentBlock = new Block();
		}
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
		if (recordsAdded > 0) {
			if (currentBlock.getRecordParts().size() > 0) {
				writeCurrentBlock();
			}
		}
		raf.close();
		if (recordsAdded > 0) {
			logger.debug("writing index");
			logger.debug("number of blocks: " + getIndex().getEntries().size());
			ObjectOutputStream oos = new ObjectOutputStream(
					Files.newOutputStream(fileIndex));
			oos.writeObject(getIndex());
			oos.close();
		}
	}

	private void writeCurrentBlock() throws IOException
	{
		currentBlock.write(raf);
		int size = currentBlock.getRecordParts().size();
		RecordPart min = currentBlock.getRecordParts().get(0);
		RecordPart max = currentBlock.getRecordParts().get(size - 1);
		getIndex().addEntry(new Entry(min.getId(), min.getIndex(), max.getId(),
				max.getIndex(),
				getIndex().getEntries().size() * (long) Block.BLOCKBYTES));
	}

	private Index getIndex()
	{
		return index;
	}

	/**
	 * Get the number of elements in this database.
	 * 
	 * @return the number of elements stored.
	 */
	public int getNumberOfElements()
	{
		return index.getEntries().size();
	}

	public boolean contains(long id)
	{
		Entry found = index.find(id);
		return found != null;
	}

	/**
	 * Find the entry identified by the given id.
	 * 
	 * @param id
	 *            the identifier used to locate the entry.
	 * @return the found entry or null if not available.
	 * @throws IOException
	 *             on IO errors.
	 */
	public T find(long id) throws IOException
	{
		Entry found = index.find(id);
		if (found == null) {
			logger.debug("unable to find index entry");
			return null;
		}

		List<RecordPart> parts = new ArrayList<>();

		logger.debug("found entry at position: "
				+ String.format("0x%x", found.getPosition()));
		Block block = blockCache.getBlock(found.getPosition());
		// Block block = Block.read(raf, found.getPosition());
		RecordPart part = block.find(id);
		logger.debug("found part: " + part);
		int nbytes = 0;
		if (part == null) {
			return null;
		}
		parts.add(part);
		nbytes += part.getLength();
		while (part.getIndex() > 0) {
			block = blockCache.getBlock(
					found.getPosition() + parts.size() * Block.BLOCKBYTES);
			// block = Block.read(raf, found.getPosition() + parts.size() *
			// Block.BLOCKBYTES);
			part = block.find(id);
			parts.add(part);
			nbytes += part.getLength();
		}

		logger.debug("number of consecutive parts found: " + parts.size());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(nbytes);
		for (RecordPart p : parts) {
			baos.write(p.getBytes());
		}

		byte[] bytes = baos.toByteArray();

		Record fromBytes = instance.fromBytes(id,
				new ByteArrayInputStream(bytes), nbytes);
		// return Unchecked.cast(fromBytes);
		return (T) fromBytes;
	}
}

class RafBlockProvider implements BlockProvider<Block>
{

	private RandomAccessFile raf;

	public RafBlockProvider(RandomAccessFile raf)
	{
		this.raf = raf;
	}

	@Override
	public Block getBlock(long pos) throws IOException
	{
		return Block.read(raf, pos);
	}

}

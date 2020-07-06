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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.diskstorage.HighLevelInputStream;
import de.topobyte.osm4j.diskstorage.HighLevelOutputStream;

/**
 * A block in the random access file that stores a number of record parts
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Block
{

	final static Logger logger = LoggerFactory.getLogger(Block.class);

	/**
	 * The number of bytes per block.
	 */
	public static final int BLOCKBYTES = 4096;
	/**
	 * The number of bytes that can be used for records.
	 */
	public static final int BLOCKBYTES_USER = BLOCKBYTES - 2;
	/**
	 * The number of bytes used for each record part as a preamble.
	 */
	public static final int MGMT_BYTES = 12;

	private List<RecordPart> parts = new ArrayList<>();

	// number of bytes per block - bytes for a short for number of entries
	private int capacityLeft = BLOCKBYTES_USER;

	/**
	 * @return the number of bytes in this block still available.
	 */
	public int getCapacity()
	{
		return capacityLeft;
	}

	/**
	 * @return the real capacity is the number of bytes theoretically available
	 *         minus the number of bytes used for managing another part for this
	 *         block. This number thus represents the number of bytes of user
	 *         data that could possibly be stored in this block.
	 */
	public int getRealCapacity()
	{
		return capacityLeft - MGMT_BYTES;
	}

	/**
	 * Add a record to this block
	 * 
	 * @param part
	 *            the record part to add
	 */
	public void add(RecordPart part)
	{
		getRecordParts().add(part);
		// number of bytes for the part + mgmt bytes
		capacityLeft -= MGMT_BYTES + part.getLength();
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

		int written = 2;
		hlos.writeShort(getRecordParts().size());
		for (RecordPart recordPart : getRecordParts()) {
			byte[] bytes = recordPart.getBytes();
			hlos.writeLong(recordPart.getId());
			written += 8;
			hlos.writeShort(recordPart.getTotal() - recordPart.getIndex() - 1);
			written += 2;
			hlos.writeShort(bytes.length);
			logger.debug("number of part bytes:" + bytes.length);
			written += 2;
			hlos.write(bytes);
			written += bytes.length;
		}
		int left = BLOCKBYTES - written;
		for (int i = 0; i < left; i++) {
			hlos.writeByte(0);
		}

		byte[] bytes = baos.toByteArray();
		hlos.close();
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
		byte[] allBytes = new byte[BLOCKBYTES];
		raf.read(allBytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(allBytes);
		HighLevelInputStream hlis = new HighLevelInputStream(bais);

		int size = hlis.readShort();
		Block block = new Block();
		for (int i = 0; i < size; i++) {
			long id = hlis.readLong();
			short index = hlis.readShort();
			short len = hlis.readShort();
			byte[] bytes = new byte[len];
			hlis.read(bytes);
			RecordPart part = new RecordPart(id, index, 0, bytes);
			block.getRecordParts().add(part);
		}
		for (RecordPart part : block.getRecordParts()) {
			part.setTotal(block.getRecordParts().size());
		}
		bais.close();
		hlis.close();
		return block;
	}

	/**
	 * Within this block, find the record with the given id.
	 * 
	 * @param id
	 *            the record's id to look for.
	 * @return the found record or null if it cannot be found.
	 */
	public RecordPart find(long id)
	{
		int i = Collections.binarySearch(getRecordParts(),
				new RecordPart(id, 0, 0, null), new Comparator<RecordPart>() {

					@Override
					public int compare(RecordPart a, RecordPart b)
					{
						if (a.getId() == b.getId()) {
							// logger.debug(a.getIndex() + ":" + b.getIndex());
							return 0;
						} else if (a.getId() > b.getId()) {
							return 1;
						} else {
							return -1;
						}
					}
				});
		if (i >= 0) {
			return getRecordParts().get(i);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return String.format("block. records: " + getRecordParts().size());
	}

	/**
	 * Getter for record parts.
	 * 
	 * @return the list of record parts in this block.
	 */
	public List<RecordPart> getRecordParts()
	{
		return parts;
	}

}

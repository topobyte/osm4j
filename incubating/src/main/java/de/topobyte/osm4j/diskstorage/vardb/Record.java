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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public abstract class Record
{

	final static Logger logger = LoggerFactory.getLogger(Record.class);

	/**
	 * @return this records id.
	 */
	public abstract long getId();

	/**
	 * @return the number of bytes this record requires.
	 */
	public abstract int getNumberOfBytes();

	/**
	 * @param stream
	 *            write this record to this OutputStream.
	 * @throws IOException
	 *             if an error occurs during writing.
	 */
	public abstract void writeTo(OutputStream stream) throws IOException;

	/**
	 * @param id
	 *            the id of the created instance.
	 * @param stream
	 *            the stream to build this from.
	 * @param nbytes
	 *            the number of bytes to read from stream.
	 * @return the constructed record.
	 * @throws IOException
	 *             if an error occurs during reading.
	 */
	public abstract Record fromBytes(long id, InputStream stream, int nbytes)
			throws IOException;

	/**
	 * Split this record into chunks.
	 * 
	 * @param initialSize
	 *            the size of the first chunk
	 * @param blockSize
	 *            the size of subsequent chunks.
	 * @return a list of record parts that sum up to the original record.
	 */
	public List<RecordPart> toRecordParts(int initialSize, int blockSize)
	{
		ArrayList<RecordPart> parts = new ArrayList<>();
		int nob = getNumberOfBytes();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(nob);
		try {
			writeTo(baos);
		} catch (IOException e) {
			logger.error("unable to write record to byte array");
		}
		byte[] bytes = baos.toByteArray();
		if (nob <= initialSize) {
			// catch easy case early
			parts.add(new RecordPart(getId(), 0, 1, bytes));
			return parts;
		}
		int partNumber = -1;
		int pos = 0;
		int len = initialSize;
		while (true) {
			partNumber++;
			byte[] partBytes = Arrays.copyOfRange(bytes, pos, pos + len);
			parts.add(new RecordPart(getId(), partNumber, 0, partBytes));
			pos += len;
			len = blockSize;
			if (pos + len > nob) {
				len = nob - pos;
			}
			if (pos >= nob) {
				break;
			}
		}
		for (RecordPart part : parts) {
			part.setTotal(parts.size());
		}
		return parts;
	}

}

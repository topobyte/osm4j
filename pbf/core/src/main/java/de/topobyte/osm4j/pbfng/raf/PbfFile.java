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

package de.topobyte.osm4j.pbfng.raf;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import de.topobyte.osm4j.pbfng.Constants;
import de.topobyte.osm4j.pbfng.util.BlockData;
import de.topobyte.osm4j.pbfng.util.BlockHeader;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public class PbfFile
{

	private RandomAccessFile file;

	private boolean blockIndexInitialized = false;
	private BlockInfo headerBlockInfo;
	private List<BlockInfo> dataBlockInfos = new ArrayList<>();

	public PbfFile(File file) throws FileNotFoundException
	{
		this(new RandomAccessFile(file, "r"));
	}

	public PbfFile(RandomAccessFile file)
	{
		this.file = file;
	}

	public void buildBlockIndex() throws IOException
	{
		headerBlockInfo = null;
		dataBlockInfos.clear();

		file.seek(0);

		while (true) {
			try {
				long pos = file.getFilePointer();
				int headerSize = file.readInt();
				BlockHeader header = PbfUtil.parseHeader(file, headerSize);
				file.skipBytes(header.getDataLength());
				BlockInfo info = new BlockInfo(pos, headerSize,
						header.getDataLength());

				if (header.getType().equals(Constants.BLOCK_TYPE_DATA)) {
					dataBlockInfos.add(info);
				} else if (header.getType().equals(Constants.BLOCK_TYPE_DATA)) {
					if (headerBlockInfo != null) {
						headerBlockInfo = info;
					} else {
						throw new IOException("Multiple header blocks");
					}
				}
			} catch (EOFException eof) {
				break;
			}
		}

		blockIndexInitialized = true;
	}

	/*
	 * Block info
	 */

	public boolean isBlockIndexInitialized()
	{
		return blockIndexInitialized;
	}

	public int getNumberOfDataBlocks()
	{
		return dataBlockInfos.size();
	}

	public BlockInfo getDataBlockInfo(int i)
	{
		return dataBlockInfos.get(i);
	}

	/*
	 * Access to raw blocks (header data + block data)
	 */

	public byte[] getRawHeaderBlockWithHeader() throws IOException
	{
		return getRawBlockWithHeader(headerBlockInfo);
	}

	public byte[] getRawDataBlockWithHeader(int i) throws IOException
	{
		BlockInfo info = dataBlockInfos.get(i);
		return getRawBlockWithHeader(info);
	}

	private byte[] getRawBlockWithHeader(BlockInfo info) throws IOException
	{
		file.seek(info.getPosition());
		int lengthTotal = info.getLengthHeader() + info.getLengthData();
		byte[] buf = new byte[lengthTotal];
		file.readFully(buf);
		return buf;
	}

	/*
	 * Data access
	 */

	public Osmformat.HeaderBlock getHeaderBlock() throws IOException
	{
		Fileformat.Blob blob = getBlockBlob(headerBlockInfo);
		BlockData blockData = PbfUtil.getBlockData(blob);
		return Osmformat.HeaderBlock.parseFrom(blockData.getBlobData());
	}

	public BlockHeader getDataBlockHeader(int i) throws IOException
	{
		BlockInfo info = dataBlockInfos.get(i);
		// seek just after the int for header length
		file.seek(info.getPosition() + 4);
		return PbfUtil.parseHeader(file, info.getLengthHeader());
	}

	public Osmformat.PrimitiveBlock getDataBlock(int i) throws IOException
	{
		BlockInfo info = dataBlockInfos.get(i);
		Fileformat.Blob blob = getBlockBlob(info);
		BlockData blockData = PbfUtil.getBlockData(blob);

		Osmformat.PrimitiveBlock primBlock = Osmformat.PrimitiveBlock
				.parseFrom(blockData.getBlobData());
		return primBlock;
	}

	private Fileformat.Blob getBlockBlob(BlockInfo info) throws IOException
	{
		file.seek(info.getPosition() + 4 + info.getLengthHeader());
		return PbfUtil.parseBlock(file, info.getLengthData());
	}

}

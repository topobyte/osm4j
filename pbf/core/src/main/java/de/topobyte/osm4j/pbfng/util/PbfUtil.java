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

package de.topobyte.osm4j.pbfng.util;

import java.io.DataInput;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import com.google.protobuf.ByteString;

import crosby.binary.Fileformat;
import crosby.binary.Osmformat;
import de.topobyte.osm4j.core.model.impl.Bound;
import de.topobyte.osm4j.pbfng.Constants;

public class PbfUtil
{

	public static Osmformat.HeaderBlock createHeader(String writingProgram,
			boolean requiresDense, Bound bound)
	{
		Osmformat.HeaderBlock.Builder headerblock = Osmformat.HeaderBlock
				.newBuilder();

		if (bound != null) {
			Osmformat.HeaderBBox.Builder bbox = Osmformat.HeaderBBox
					.newBuilder();
			bbox.setLeft(bboxDegreesToLong(bound.getLeft()));
			bbox.setBottom(bboxDegreesToLong(bound.getBottom()));
			bbox.setRight(bboxDegreesToLong(bound.getRight()));
			bbox.setTop(bboxDegreesToLong(bound.getTop()));
			headerblock.setBbox(bbox);
		}

		headerblock.setWritingprogram(writingProgram);
		headerblock.addRequiredFeatures(Constants.FEATURE_SCHEMA_0_6);
		if (requiresDense) {
			headerblock.addRequiredFeatures(Constants.FEATURE_DENSE_NODES);
		}
		return headerblock.build();
	}

	private static long bboxDegreesToLong(double value)
	{
		return (long) (value / .000000001);
	}

	public static double bboxLongToDegrees(long value)
	{
		return value * .000000001;
	}

	public static BlockHeader parseHeader(DataInput input) throws IOException
	{
		int lengthHeader = input.readInt();
		return parseHeader(input, lengthHeader);
	}

	public static BlockHeader parseHeader(DataInput input, int lengthHeader)
			throws IOException
	{
		byte buf[] = new byte[lengthHeader];
		input.readFully(buf);

		Fileformat.BlobHeader header = Fileformat.BlobHeader.parseFrom(buf);
		BlockHeader h = new BlockHeader(header.getType(), header.getDatasize(),
				header.getIndexdata());

		return h;
	}

	public static Fileformat.Blob parseBlock(DataInput data, int lengthData)
			throws IOException
	{
		byte buf[] = new byte[lengthData];
		data.readFully(buf);

		Fileformat.Blob blob = Fileformat.Blob.parseFrom(buf);
		return blob;
	}

	private static LZ4FastDecompressor lz4Decompressor = null;

	private static void initLz4()
	{
		if (lz4Decompressor == null) {
			LZ4Factory factory = LZ4Factory.fastestInstance();
			lz4Decompressor = factory.fastDecompressor();
		}
	}

	public static ByteString getBlockData(Fileformat.Blob blob)
			throws IOException
	{
		ByteString blobData;
		if (blob.hasRaw()) {
			blobData = blob.getRaw();
		} else if (blob.hasZlibData()) {
			byte uncompressed[] = new byte[blob.getRawSize()];

			Inflater decompresser = new Inflater();
			decompresser.setInput(blob.getZlibData().toByteArray());
			try {
				decompresser.inflate(uncompressed);
			} catch (DataFormatException e) {
				throw new IOException("Error while decompressing gzipped data",
						e);
			}
			decompresser.end();

			blobData = ByteString.copyFrom(uncompressed);
		} else if (blob.hasLz4Data()) {
			byte uncompressed[] = new byte[blob.getRawSize()];

			initLz4();
			lz4Decompressor.decompress(blob.getLz4Data().toByteArray(), 0,
					uncompressed, 0, blob.getRawSize());

			blobData = ByteString.copyFrom(uncompressed);
		} else {
			throw new IOException("Encountered block without data");
		}

		return blobData;
	}

}

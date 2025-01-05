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

package de.topobyte.osm4j.pbf.seq;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

import com.google.protobuf.ByteString;

import de.topobyte.osm4j.pbf.Compression;
import de.topobyte.osm4j.pbf.protobuf.Fileformat;

public class BlockWriter
{

	private DataOutputStream output;

	public BlockWriter(OutputStream output)
	{
		this.output = new DataOutputStream(output);
	}

	public void write(String type, ByteString indexData,
			Compression compression, ByteString data) throws IOException
	{
		Fileformat.Blob.Builder blobBuilder = Fileformat.Blob.newBuilder();
		switch (compression) {
		default:
		case NONE:
			blobBuilder.setRaw(data);
			blobBuilder.setRawSize(data.size());
			break;
		case DEFLATE:
			blobBuilder.setRawSize(data.size());
			ByteArray compressed = deflate(data);
			ByteString zlibData = ByteString.copyFrom(compressed.getData(), 0,
					compressed.getLength());
			blobBuilder.setZlibData(zlibData);
			break;
		case LZ4:
			blobBuilder.setRawSize(data.size());
			compressed = lz4(data);
			ByteString lz4Data = ByteString.copyFrom(compressed.getData(), 0,
					compressed.getLength());
			blobBuilder.setLz4Data(lz4Data);
			break;
		}
		Fileformat.Blob blob = blobBuilder.build();
		write(type, indexData, blob);
	}

	public void write(String type, ByteString indexData, Fileformat.Blob blob)
			throws IOException
	{
		Fileformat.BlobHeader.Builder headerBuilder = Fileformat.BlobHeader
				.newBuilder();

		if (indexData != null) {
			headerBuilder.setIndexdata(indexData);
		}
		headerBuilder.setType(type);
		headerBuilder.setDatasize(blob.getSerializedSize());
		Fileformat.BlobHeader header = headerBuilder.build();
		int size = header.getSerializedSize();

		output.writeInt(size);
		header.writeTo(output);
		blob.writeTo(output);
	}

	protected ByteArray deflate(ByteString data)
	{
		int size = data.size();

		Deflater deflater = new Deflater();
		deflater.setInput(data.toByteArray());
		deflater.finish();

		byte out[] = new byte[size];
		deflater.deflate(out);

		if (!deflater.finished()) {
			out = Arrays.copyOf(out, size + size / 64 + 16);
			deflater.deflate(out, deflater.getTotalOut(),
					out.length - deflater.getTotalOut());
			if (!deflater.finished()) {
				throw new Error("Internal error in compressor");
			}
		}

		int length = deflater.getTotalOut();
		deflater.end();

		return new ByteArray(out, length);
	}

	private LZ4Compressor lz4Compressor = null;

	private void initLz4()
	{
		if (lz4Compressor == null) {
			LZ4Factory factory = LZ4Factory.fastestInstance();
			lz4Compressor = factory.fastCompressor();
		}
	}

	protected ByteArray lz4(ByteString data)
	{
		initLz4();

		int size = data.size();

		int estimate = lz4Compressor.maxCompressedLength(size);
		byte[] out = new byte[estimate];
		int length = lz4Compressor.compress(data.toByteArray(), 0, size, out, 0,
				estimate);

		return new ByteArray(out, length);
	}

}

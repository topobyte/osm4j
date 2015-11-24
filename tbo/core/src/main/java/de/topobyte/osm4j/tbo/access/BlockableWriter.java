package de.topobyte.osm4j.tbo.access;

import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import de.topobyte.compactio.CompactWriter;
import de.topobyte.compactio.OutputStreamCompactWriter;
import de.topobyte.osm4j.tbo.ByteArrayOutputStream;
import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.data.FileBlock;
import de.topobyte.osm4j.tbo.writerhelper.Blockable;

public class BlockableWriter
{

	private BlockWriter blockWriter;

	private ByteArrayOutputStream baos;

	private boolean lowMemoryFootprint;

	public BlockableWriter(BlockWriter blockWriter)
	{
		this(blockWriter, false);
	}

	public BlockableWriter(BlockWriter blockWriter, boolean lowMemoryFootprint)
	{
		this.blockWriter = blockWriter;
		this.lowMemoryFootprint = lowMemoryFootprint;
		if (!lowMemoryFootprint) {
			baos = new ByteArrayOutputStream();
		}
	}

	public void writeBlock(Blockable blockable, int type, int count,
			Compression compression) throws IOException
	{
		if (lowMemoryFootprint) {
			baos = new ByteArrayOutputStream();
		} else {
			baos.reset();
		}

		CompactWriter bufferWriter = new OutputStreamCompactWriter(baos);
		blockable.write(bufferWriter);
		byte[] uncompressed = baos.toByteArray();
		byte[] compressed = null;
		int length = 0;
		baos.reset();

		switch (compression) {
		default:
		case NONE:
			compressed = uncompressed;
			length = compressed.length;
			break;
		case DEFLATE:
			DeflaterOutputStream out = new DeflaterOutputStream(baos);
			out.write(uncompressed);
			out.close();
			compressed = baos.toByteArray();
			length = compressed.length;
			break;
		case LZ4:
			initLz4();
			int estimate = lz4Compressor
					.maxCompressedLength(uncompressed.length);
			compressed = new byte[estimate];
			length = lz4Compressor.compress(uncompressed, compressed);
			break;
		}

		FileBlock block = new FileBlock(type, compression, uncompressed.length,
				count, compressed, length);
		blockWriter.writeBlock(block);

		if (lowMemoryFootprint) {
			baos = null;
		}
	}

	private LZ4Compressor lz4Compressor = null;

	private void initLz4()
	{
		if (lz4Compressor == null) {
			LZ4Factory factory = LZ4Factory.fastestInstance();
			lz4Compressor = factory.fastCompressor();
		}
	}

}

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

package de.topobyte.osm4j.extra.datatree.closingstreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class TestByteArrayWriter
{

	private ByteArrayGenerator generator = new ByteArrayGenerator();
	private byte[] bytes = generator.generateBytes(1024);

	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private ByteArrayWriter writer = new ByteArrayWriter(bytes, baos);

	private Random random = new Random();

	@Test
	public void testWriteByte() throws IOException
	{
		while (!writer.done()) {
			writer.writeByte();
		}

		Assert.assertArrayEquals(bytes, baos.toByteArray());
	}

	@Test
	public void testWriteBytes1() throws IOException
	{
		while (!writer.done()) {
			int len = random.nextInt(Math.min(10, writer.remaining() + 1));
			writer.writeBytes(len);
		}

		Assert.assertArrayEquals(bytes, baos.toByteArray());
	}

	@Test
	public void testWriteBytes2() throws IOException
	{
		while (!writer.done()) {
			int len = random.nextInt(Math.min(10, writer.remaining() + 1));
			int pad1 = random.nextInt(100);
			int pad2 = random.nextInt(100);
			writer.writeBytes(len, pad1, pad2);
		}

		Assert.assertArrayEquals(bytes, baos.toByteArray());
	}

	@Test
	public void testAll() throws IOException
	{
		int i = 0;
		while (!writer.done()) {
			int mod = i++ % 3;
			if (mod == 0) {
				writer.writeByte();
			} else if (mod == 1) {
				int len = random.nextInt(Math.min(10, writer.remaining() + 1));
				writer.writeBytes(len);
			} else if (mod == 2) {
				int len = random.nextInt(Math.min(10, writer.remaining() + 1));
				int pad1 = random.nextInt(100);
				int pad2 = random.nextInt(100);
				writer.writeBytes(len, pad1, pad2);
			}
		}

		Assert.assertArrayEquals(bytes, baos.toByteArray());
	}

}

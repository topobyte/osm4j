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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.osm4j.extra.datatree.ClosingFileOutputStream;
import de.topobyte.osm4j.extra.datatree.ClosingFileOutputStreamFactory;
import de.topobyte.osm4j.extra.datatree.ClosingFileOutputStreamPool;

public class TestClosingFileOutputStream
{

	private File[] files;

	@After
	public void cleanup() throws IOException
	{
		for (File file : files) {
			file.delete();
		}
	}

	@Test
	public void test() throws IOException
	{
		test(1);
		test(2);
		test(10);
	}

	public void test(int n) throws IOException
	{
		files = new File[n];
		for (int i = 0; i < n; i++) {
			files[i] = File.createTempFile("closing-fos", ".dat");
		}

		ByteArrayGenerator generator = new ByteArrayGenerator();
		byte[][] bytes = new byte[n][];
		for (int i = 0; i < n; i++) {
			bytes[i] = generator.generateBytes(1024);
		}

		ClosingFileOutputStreamFactory factory = new ClosingFileOutputStreamPool();

		OutputStream[] outputs = new OutputStream[n];
		for (int i = 0; i < n; i++) {
			outputs[i] = new ClosingFileOutputStream(factory, files[i], i);
		}

		WriterUtil.writeInterleaved(outputs, bytes);

		for (int i = 0; i < n; i++) {
			outputs[i].close();
		}

		for (int i = 0; i < n; i++) {
			byte[] read = FileUtils.readFileToByteArray(files[i]);
			Assert.assertArrayEquals(bytes[i], read);
		}
	}

}

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

package de.topobyte.largescalefileio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.largescalefileio.ClosingFileOutputStream;
import de.topobyte.largescalefileio.ClosingFileOutputStreamPool;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamPool;

public class TestClosingFileOutputStream
{

	private Set<File> allFiles = new HashSet<>();
	private File[] files;

	@After
	public void cleanup() throws IOException
	{
		for (File file : allFiles) {
			file.delete();
		}
	}

	@Test
	public void test() throws IOException
	{
		test(1, false);
		test(2, false);
		test(10, false);
		test(1, true);
		test(2, true);
		test(10, true);
	}

	public void test(int n, boolean existingFiles) throws IOException
	{
		files = new File[n];
		for (int i = 0; i < n; i++) {
			files[i] = File.createTempFile("closing-fos", ".dat");
		}
		allFiles.addAll(Arrays.asList(files));

		ByteArrayGenerator generator = new ByteArrayGenerator();
		byte[][] bytes = new byte[n][];
		for (int i = 0; i < n; i++) {
			bytes[i] = generator.generateBytes(1024);
		}

		if (existingFiles) {
			for (int i = 0; i < n; i++) {
				writeSomeData(files[i], generator);
			}
		}

		ClosingFileOutputStreamPool factory = new SimpleClosingFileOutputStreamPool();

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

	@Test
	@SuppressWarnings("resource")
	public void testOpenTruncatesFile() throws IOException
	{
		File file = File.createTempFile("closing-fos", ".dat");
		allFiles.add(file);

		ByteArrayGenerator generator = new ByteArrayGenerator();
		writeSomeData(file, generator);

		SimpleClosingFileOutputStreamPool factory = new SimpleClosingFileOutputStreamPool();
		new ClosingFileOutputStream(factory, file, 0);

		byte[] read = FileUtils.readFileToByteArray(file);
		Assert.assertEquals(0, read.length);
	}

	private void writeSomeData(File file, ByteArrayGenerator generator)
			throws IOException
	{
		byte[] bytes = generator.generateBytes(100);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(bytes);
		fos.close();
	}

}

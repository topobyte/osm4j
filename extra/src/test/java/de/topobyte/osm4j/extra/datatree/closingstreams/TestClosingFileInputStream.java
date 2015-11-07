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
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.topobyte.largescalefileio.ClosingFileInputStream;
import de.topobyte.largescalefileio.ClosingFileInputStreamPool;
import de.topobyte.largescalefileio.SimpleClosingFileInputStreamPool;

public class TestClosingFileInputStream
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
		test(1);
		test(2);
		test(10);
	}

	public void test(int n) throws IOException
	{
		files = new File[n];
		for (int i = 0; i < n; i++) {
			files[i] = File.createTempFile("closing-fis", ".dat");
		}
		allFiles.addAll(Arrays.asList(files));

		ByteArrayGenerator generator = new ByteArrayGenerator();
		byte[][] bytes = new byte[n][];
		for (int i = 0; i < n; i++) {
			bytes[i] = generator.generateBytes(1024);
		}

		for (int i = 0; i < n; i++) {
			FileUtils.writeByteArrayToFile(files[i], bytes[i]);
		}

		ClosingFileInputStreamPool factory = new SimpleClosingFileInputStreamPool();

		InputStream[] inputs = new InputStream[n];
		for (int i = 0; i < n; i++) {
			inputs[i] = new ClosingFileInputStream(factory, files[i], i);
		}

		byte[][] results = ReaderUtil.readInterleaved(inputs);

		for (int i = 0; i < n; i++) {
			Assert.assertArrayEquals(bytes[i], results[i]);
		}
	}

}

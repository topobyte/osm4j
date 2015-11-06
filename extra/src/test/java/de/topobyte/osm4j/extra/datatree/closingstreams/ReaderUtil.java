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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReaderUtil
{

	public static byte[][] readInterleaved(InputStream[] inputs)
			throws IOException
	{
		int n = inputs.length;
		byte[][] results = new byte[n][];
		ByteArrayOutputStream[] outputs = new ByteArrayOutputStream[n];

		Random random = new Random();

		List<ByteArrayReader> readers = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			outputs[i] = new ByteArrayOutputStream();
			readers.add(new ByteArrayReader(outputs[i], inputs[i]));
		}

		while (!readers.isEmpty()) {
			int i = random.nextInt(readers.size());
			ByteArrayReader reader = readers.get(i);

			int mod = random.nextInt(2);
			if (mod == 0) {
				reader.read();
			} else if (mod == 1) {
				int len = random.nextInt(10);
				reader.read(len);
			}

			if (reader.done()) {
				readers.remove(i);
			}
		}

		for (int i = 0; i < n; i++) {
			results[i] = outputs[i].toByteArray();
		}
		return results;
	}

}

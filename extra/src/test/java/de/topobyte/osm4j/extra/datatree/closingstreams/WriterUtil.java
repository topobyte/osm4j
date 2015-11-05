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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WriterUtil
{

	public static void writeInterleaved(OutputStream[] outputs, byte[][] bytes)
			throws IOException
	{
		Random random = new Random();

		int n = outputs.length;
		List<ByteArrayWriter> writers = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			writers.add(new ByteArrayWriter(bytes[i], outputs[i]));
		}

		while (!writers.isEmpty()) {
			int i = random.nextInt(writers.size());
			ByteArrayWriter writer = writers.get(i);

			int mod = random.nextInt(3);
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

			if (writer.done()) {
				writers.remove(i);
			}
		}
	}

}

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

package de.topobyte.osm4j.pbf.executables;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.topobyte.osm4j.pbf.Constants;
import de.topobyte.osm4j.pbf.protobuf.Fileformat;
import de.topobyte.osm4j.pbf.protobuf.Osmformat;
import de.topobyte.osm4j.pbf.util.BlobHeader;
import de.topobyte.osm4j.pbf.util.BlockData;
import de.topobyte.osm4j.pbf.util.PbfUtil;

public class HeaderInfo
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println("usage: " + HeaderInfo.class.getSimpleName()
					+ " <filename>");
			System.exit(1);
		}

		InputStream input = new FileInputStream(args[0]);
		DataInputStream data = new DataInputStream(input);

		boolean passedFirst = false;
		boolean warningShown = false;
		while (true) {
			if (passedFirst && !warningShown) {
				System.out.println("Warning: first block is not a header!");
				System.out
						.println("... will continue reading until I find one.");
				warningShown = true;
			}
			try {
				BlobHeader header = PbfUtil.parseHeader(data);

				Fileformat.Blob blob = PbfUtil.parseBlock(data,
						header.getDataLength());
				BlockData blockData = PbfUtil.getBlockData(blob);

				String type = header.getType();

				passedFirst = true;

				if (type.equals(Constants.BLOCK_TYPE_HEADER)) {
					Osmformat.HeaderBlock headerBlock = Osmformat.HeaderBlock
							.parseFrom(blockData.getBlobData());
					printHeaderInfo(headerBlock);
					return;
				}

			} catch (EOFException eof) {
				if (!passedFirst) {
					System.out.println("End of file before header");
				}
				break;
			}
		}

	}

	private static void printHeaderInfo(Osmformat.HeaderBlock header)
	{
		if (header.hasWritingprogram()) {
			System.out
					.println("Writing program: " + header.getWritingprogram());
		} else {
			System.out.println("No writing program information.");
		}

		for (int i = 0; i < header.getRequiredFeaturesCount(); i++) {
			System.out.println("Required feature: "
					+ header.getRequiredFeatures(i));
		}

		for (int i = 0; i < header.getOptionalFeaturesCount(); i++) {
			System.out.println("Optional feature: "
					+ header.getOptionalFeatures(i));
		}

		if (header.hasSource()) {
			System.out.println("Source: " + header.getSource());
		} else {
			System.out.println("No source given.");
		}

		if (header.hasBbox()) {
			Osmformat.HeaderBBox bbox = header.getBbox();
			double left = PbfUtil.bboxLongToDegrees(bbox.getLeft());
			double right = PbfUtil.bboxLongToDegrees(bbox.getRight());
			double top = PbfUtil.bboxLongToDegrees(bbox.getTop());
			double bottom = PbfUtil.bboxLongToDegrees(bbox.getBottom());
			System.out.println(String.format("Bounding box: %f,%f,%f,%f", left,
					bottom, right, top));
		} else {
			System.out.println("No bbox given.");
		}
	}

}

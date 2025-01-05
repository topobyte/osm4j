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

import java.io.File;
import java.io.IOException;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbf.raf.FileStructure;
import de.topobyte.osm4j.pbf.raf.FileStructureAnalyzer;
import de.topobyte.osm4j.pbf.raf.Interval;
import de.topobyte.osm4j.pbf.raf.PbfFile;

public class FindEntityBlockIntervals
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1) {
			System.out.println(
					"usage: " + FindEntityBlockIntervals.class.getSimpleName()
							+ " <filename>");
			System.exit(1);
		}

		File file = new File(args[0]);

		PbfFile pbfFile = new PbfFile(file);
		pbfFile.buildBlockIndex();

		FileStructure fileStructure = FileStructureAnalyzer.analyze(pbfFile);

		for (EntityType type : EntityType.values()) {
			if (!fileStructure.hasType(type)) {
				System.out.println(type + ": none");
			} else {
				Interval blocks = fileStructure.getBlocks(type);
				System.out.println(String.format(type + ": [%d, %d]",
						blocks.getStart(), blocks.getEnd()));
			}
		}
	}

}

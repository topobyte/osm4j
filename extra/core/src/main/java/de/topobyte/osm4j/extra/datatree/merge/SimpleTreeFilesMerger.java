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

package de.topobyte.osm4j.extra.datatree.merge;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class SimpleTreeFilesMerger extends AbstractTreeFilesMerger
{

	public SimpleTreeFilesMerger(Path pathTree, List<String> fileNamesSorted,
			List<String> fileNamesUnsorted, String fileNamesOutput,
			FileFormat inputFormat, OsmOutputConfig outputConfig,
			boolean deleteInput)
	{
		super(pathTree, fileNamesSorted, fileNamesUnsorted, fileNamesOutput,
				inputFormat, outputConfig, deleteInput);
	}

	@Override
	public void execute() throws IOException
	{
		prepare();

		run();
	}

	public void run() throws IOException
	{
		int i = 0;
		for (Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			mergeFiles(leaf);

			stats(i);
		}
	}

}

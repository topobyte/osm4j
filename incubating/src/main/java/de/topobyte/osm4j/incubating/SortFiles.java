// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.incubating;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.sort.MemorySort;

public class SortFiles
{

	private Path path;

	public SortFiles(Path path)
	{
		this.path = path;
	}

	public void execute() throws IOException
	{
		List<Path> paths = find("input.tbo");
		int i = 0;
		for (Path path : paths) {
			System.out.println(String.format("%d / %d: %s", ++i, paths.size(),
					path));
			Path opath = path.resolveSibling("sorted.tbo");

			OsmFileInput input = new OsmFileInput(path, FileFormat.TBO);
			OsmOutputConfig c = new OsmOutputConfig(FileFormat.TBO);
			OutputStream output = StreamUtil.bufferedOutputStream(opath);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output, c);

			OsmIteratorInput iteratorInput = input.createIterator(true, true);
			MemorySort memorySort = new MemorySort(osmOutput,
					iteratorInput.getIterator());
			memorySort.run();

			iteratorInput.close();
			output.close();
		}
	}

	private List<Path> find(final String name) throws IOException
	{
		final List<Path> results = new ArrayList<>();
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException
			{
				String fname = file.getFileName().toString();
				if (name.equals(fname)) {
					results.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

		};
		Files.walkFileTree(path, visitor);
		return results;
	}

}

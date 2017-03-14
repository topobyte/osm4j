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

package de.topobyte.osm4j.extra;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Renamer
{

	private Path path;
	private String from;
	private String to;
	private boolean dry;

	public Renamer(Path path, String from, String to, boolean dry)
	{
		this.path = path;
		this.from = from;
		this.to = to;
		this.dry = dry;
	}

	public void execute() throws IOException
	{
		List<Path> paths = find(from);
		int i = 0;
		for (Path source : paths) {
			System.out.println(String.format("%d / %d: %s", ++i, paths.size(),
					source));
			Path target = source.resolveSibling(to);
			System.out.println(source + " -> " + target);

			if (Files.exists(target)) {
				System.out.println("target exists");
			}
			if (dry) {
				continue;
			}
			Files.move(source, target);
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

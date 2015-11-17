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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.nio.file.Path;

public class DataTreeFiles
{

	private File dirFile;
	private Path pathFile;
	private String filename;

	public DataTreeFiles(File dir, String filename)
	{
		this.pathFile = dir.toPath();
		this.dirFile = dir;
		this.filename = filename;
	}

	public DataTreeFiles(Path dir, String filename)
	{
		this.pathFile = dir;
		this.dirFile = dir.toFile();
		this.filename = filename;
	}

	public File getFile(Node leaf)
	{
		File subdir = new File(dirFile, Long.toHexString(leaf.getPath()));
		return new File(subdir, filename);
	}

	public Path getPath(Node leaf)
	{
		Path subdir = pathFile.resolve(Long.toHexString(leaf.getPath()));
		return subdir.resolve(filename);
	}

}

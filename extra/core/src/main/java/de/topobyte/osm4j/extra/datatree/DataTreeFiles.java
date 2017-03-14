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
	private Path dirPath;
	private String filename;

	public DataTreeFiles(File dir, String filename)
	{
		this.dirPath = dir.toPath();
		this.dirFile = dir;
		this.filename = filename;
	}

	public DataTreeFiles(Path dir, String filename)
	{
		this.dirPath = dir;
		this.dirFile = dir.toFile();
		this.filename = filename;
	}

	public Path getSubdirPath(Node leaf)
	{
		return dirPath.resolve(Long.toHexString(leaf.getPath()));
	}

	public File getSubdirFile(Node leaf)
	{
		return new File(dirFile, Long.toHexString(leaf.getPath()));
	}

	public File getFile(Node leaf)
	{
		File subdir = new File(dirFile, Long.toHexString(leaf.getPath()));
		return new File(subdir, filename);
	}

	public Path getPath(Node leaf)
	{
		Path subdir = dirPath.resolve(Long.toHexString(leaf.getPath()));
		return subdir.resolve(filename);
	}

	public File getFile(long leafPath)
	{
		File subdir = new File(dirFile, Long.toHexString(leafPath));
		return new File(subdir, filename);
	}

	public Path getPath(long leafPath)
	{
		Path subdir = dirPath.resolve(Long.toHexString(leafPath));
		return subdir.resolve(filename);
	}

}

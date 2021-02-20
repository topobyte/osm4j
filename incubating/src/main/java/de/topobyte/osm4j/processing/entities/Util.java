// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.processing.entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Util
{

	final static Logger logger = LoggerFactory.getLogger(Util.class);

	public static void ensureExistsAndIsReadable(Path path, String name)
			throws IOException
	{
		if (!Files.exists(path)) {
			throw new IOException(
					String.format("%s does not exist: %s", name, path));
		}
		if (!Files.isReadable(path)) {
			throw new IOException(
					String.format("unable to read from %s: %s", name, path));
		}
	}

	public static void ensureDirectoryExistsAndIsWritable(Path dir, String name)
			throws IOException
	{
		if (!Files.exists(dir)) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				throw new IOException(
						String.format("unable to create %s: %s", name, dir), e);
			}
		}
		if (!Files.exists(dir)) {
			throw new IOException(
					String.format("unable to create %s: %s", name, dir));
		}
		if (!Files.isWritable(dir)) {
			throw new IOException(
					String.format("unable to write to %s: %s", name, dir));
		}
	}

}

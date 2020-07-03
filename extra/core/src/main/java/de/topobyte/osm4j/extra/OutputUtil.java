// Copyright 2020 Sebastian Kuerten
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
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputUtil
{

	final static Logger logger = LoggerFactory.getLogger(OutputUtil.class);

	public static void ensureOutputDirectory(Path pathOutput) throws IOException
	{
		if (!Files.exists(pathOutput)) {
			logger.info("Creating output directory");
			Files.createDirectories(pathOutput);
		}
		if (!Files.isDirectory(pathOutput)) {
			String error = "Output path is not a directory";
			logger.error(error);
			throw new IOException(error);
		}
		if (pathOutput.toFile().list().length != 0) {
			String error = "Output directory is not empty";
			logger.error(error);
			throw new IOException(error);
		}
	}

}

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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.access.OsmIdIteratorInput;
import de.topobyte.osm4j.core.access.OsmIdReader;
import de.topobyte.osm4j.core.access.OsmIdReaderInput;
import de.topobyte.osm4j.core.access.OsmInputAccessFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.access.OsmReaderInput;

public class OsmFileInput implements OsmInputAccessFactory
{

	private Path path;
	private FileFormat fileFormat;

	public OsmFileInput(OsmFile osmFile)
	{
		this.path = osmFile.getPath();
		this.fileFormat = osmFile.getFileFormat();
	}

	public OsmFileInput(Path path, FileFormat fileFormat)
	{
		this.path = path;
		this.fileFormat = fileFormat;
	}

	@Override
	public OsmIteratorInput createIterator(boolean readTags,
			boolean readMetadata) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, fileFormat,
				readTags, readMetadata);
		return new OsmSingleIteratorInput(input, iterator);
	}

	@Override
	public OsmReaderInput createReader(boolean readTags, boolean readMetadata)
			throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmReader reader = OsmIoUtils.setupOsmReader(input, fileFormat,
				readTags, readMetadata);
		return new OsmSingleReaderInput(input, reader);
	}

	@Override
	public OsmIdIteratorInput createIdIterator() throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIdIterator iterator = OsmIoUtils.setupOsmIdIterator(input,
				fileFormat);
		return new OsmSingleIdIteratorInput(input, iterator);
	}

	@Override
	public OsmIdReaderInput createIdReader() throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIdReader reader = OsmIoUtils.setupOsmIdReader(input, fileFormat);
		return new OsmSingleIdReaderInput(input, reader);
	}

}

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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmReader;

public class OsmStreamInput implements OsmAccessFactory
{

	private InputStream input;
	private FileFormat fileFormat;

	public OsmStreamInput(OsmStream osmStream)
	{
		this.input = osmStream.getInputStream();
		this.fileFormat = osmStream.getFileFormat();
	}

	public OsmStreamInput(InputStream input, FileFormat fileFormat)
	{
		this.input = input;
		this.fileFormat = fileFormat;
	}

	@Override
	public OsmIteratorInput createIterator(boolean readMetadata)
			throws IOException
	{
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(input, fileFormat,
				readMetadata);
		return new OsmSingleIteratorInput(input, iterator);
	}

	@Override
	public OsmReaderInput createReader(boolean readMetadata) throws IOException
	{
		OsmReader reader = OsmIoUtils.setupOsmReader(input, fileFormat,
				readMetadata);
		return new OsmSingleReaderInput(input, reader);
	}

}

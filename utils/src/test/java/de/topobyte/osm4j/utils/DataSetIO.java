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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.resolve.DataSetReader;
import de.topobyte.osm4j.core.resolve.InMemoryMapDataSet;
import de.topobyte.osm4j.testing.TestDataSet;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class DataSetIO
{

	public static void write(TestDataSet data, File file, FileFormat format,
			boolean writeMetadata) throws IOException
	{
		OutputStream fos = new FileOutputStream(file);
		OutputStream bos = new BufferedOutputStream(fos);
		PbfConfig pbfConfig = new PbfConfig();
		TboConfig tboConfig = new TboConfig();
		OsmIoUtils.setupOsmOutput(bos, format, writeMetadata, pbfConfig,
				tboConfig);
		bos.close();
	}

	public static TestDataSet read(File file, FileFormat format,
			boolean readMetadata) throws IOException
	{
		InputStream fis = new FileInputStream(file);
		InputStream bis = new BufferedInputStream(fis);
		OsmIterator iterator = OsmIoUtils.setupOsmIterator(bis, format,
				readMetadata);
		InMemoryMapDataSet data = DataSetReader
				.read(iterator, true, true, true);
		bis.close();
		return new TestDataSet(data);
	}

}

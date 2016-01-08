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

package de.topobyte.osm4j.extra.extracts.query;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryListDataSet;
import de.topobyte.osm4j.core.dataset.ListDataSetLoader;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public abstract class AbstractQuery
{

	protected FileFormat inputFormat;
	protected FileFormat outputFormat;
	protected boolean writeMetadata;
	protected PbfConfig pbfConfig;
	protected TboConfig tboConfig;

	public AbstractQuery(FileFormat inputFormat, FileFormat outputFormat,
			boolean writeMetadata, PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.inputFormat = inputFormat;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
	}

	protected String filename(int index)
	{
		return String.format("%d%s", index, OsmIoUtils.extension(outputFormat));
	}

	protected OsmStreamOutput createOutput(Path path) throws IOException
	{
		OutputStream outputStream = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutputStream = OsmIoUtils
				.setupOsmOutput(outputStream, outputFormat, writeMetadata,
						pbfConfig, tboConfig);
		return new OsmOutputStreamStreamOutput(outputStream, osmOutputStream);
	}

	protected void finish(OsmStreamOutput osmOutput) throws IOException
	{
		osmOutput.getOsmOutput().complete();
		osmOutput.close();
	}

	protected InMemoryListDataSet read(Path path) throws IOException
	{
		OsmFileInput fileInput = new OsmFileInput(path, inputFormat);
		OsmIteratorInput input = fileInput.createIterator(true, writeMetadata);
		InMemoryListDataSet data = ListDataSetLoader.read(input.getIterator(),
				true, true, true);
		input.close();
		return data;
	}

}

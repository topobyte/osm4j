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
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public abstract class AbstractQuery
{

	protected FileFormat inputFormat;
	protected OsmOutputConfig outputConfigIntermediate;
	protected OsmOutputConfig outputConfig;

	public AbstractQuery(FileFormat inputFormat,
			OsmOutputConfig outputConfigIntermediate,
			OsmOutputConfig outputConfig)
	{
		this.inputFormat = inputFormat;
		this.outputConfigIntermediate = outputConfigIntermediate;
		this.outputConfig = outputConfig;
	}

	protected String filename(int index)
	{
		return String.format("%d%s", index,
				OsmIoUtils.extension(outputConfigIntermediate.getFileFormat()));
	}

	protected OsmStreamOutput createOutput(Path path) throws IOException
	{
		OutputStream outputStream = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutputStream = OsmIoUtils.setupOsmOutput(
				outputStream, outputConfigIntermediate);
		return new OsmOutputStreamStreamOutput(outputStream, osmOutputStream);
	}

	protected OsmStreamOutput createFinalOutput(Path path) throws IOException
	{
		OutputStream outputStream = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutputStream = OsmIoUtils.setupOsmOutput(
				outputStream, outputConfig);
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
		OsmIteratorInput input = fileInput.createIterator(true,
				outputConfig.isWriteMetadata());
		InMemoryListDataSet data = ListDataSetLoader.read(input.getIterator(),
				true, true, true);
		input.close();
		return data;
	}

}

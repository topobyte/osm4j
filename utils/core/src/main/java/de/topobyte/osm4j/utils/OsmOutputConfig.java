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

import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class OsmOutputConfig
{

	private FileFormat fileFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata;

	public OsmOutputConfig(FileFormat outputFormat)
	{
		this(outputFormat, new PbfConfig(), new TboConfig(), true);
	}

	public OsmOutputConfig(FileFormat outputFormat, boolean writeMetadata)
	{
		this(outputFormat, new PbfConfig(), new TboConfig(), writeMetadata);
	}

	public OsmOutputConfig(FileFormat outputFormat, PbfConfig pbfConfig,
			TboConfig tboConfig, boolean writeMetadata)
	{
		this.fileFormat = outputFormat;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
		this.writeMetadata = writeMetadata;
	}

	public FileFormat getFileFormat()
	{
		return fileFormat;
	}

	public void setFileFormat(FileFormat fileFormat)
	{
		this.fileFormat = fileFormat;
	}

	public PbfConfig getPbfConfig()
	{
		return pbfConfig;
	}

	public void setPbfConfig(PbfConfig pbfConfig)
	{
		this.pbfConfig = pbfConfig;
	}

	public TboConfig getTboConfig()
	{
		return tboConfig;
	}

	public void setTboConfig(TboConfig tboConfig)
	{
		this.tboConfig = tboConfig;
	}

	public boolean isWriteMetadata()
	{
		return writeMetadata;
	}

	public void setWriteMetadata(boolean writeMetadata)
	{
		this.writeMetadata = writeMetadata;
	}

}

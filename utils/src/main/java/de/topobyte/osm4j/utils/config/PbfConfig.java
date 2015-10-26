package de.topobyte.osm4j.utils.config;

import de.topobyte.osm4j.pbf.Compression;

public class PbfConfig
{

	private Compression compression = Compression.DEFLATE;
	private boolean useDenseNodes = true;

	public Compression getCompression()
	{
		return compression;
	}

	public void setCompression(Compression compression)
	{
		this.compression = compression;
	}

	public boolean isUseDenseNodes()
	{
		return useDenseNodes;
	}

	public void setUseDenseNodes(boolean useDenseNodes)
	{
		this.useDenseNodes = useDenseNodes;
	}

}

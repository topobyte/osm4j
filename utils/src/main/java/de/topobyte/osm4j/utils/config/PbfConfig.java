package de.topobyte.osm4j.utils.config;

public class PbfConfig
{

	private boolean useCompression = true;
	private boolean useDenseNodes = true;

	public boolean isUseCompression()
	{
		return useCompression;
	}

	public void setUseCompression(boolean useCompression)
	{
		this.useCompression = useCompression;
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

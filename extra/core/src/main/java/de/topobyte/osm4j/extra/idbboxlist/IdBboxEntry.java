package de.topobyte.osm4j.extra.idbboxlist;

import org.locationtech.jts.geom.Envelope;

public class IdBboxEntry
{

	private long id;
	private Envelope envelope;
	private int size;

	public IdBboxEntry(long id, Envelope envelope, int size)
	{
		this.id = id;
		this.envelope = envelope;
		this.size = size;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Envelope getEnvelope()
	{
		return envelope;
	}

	public void setEnvelope(Envelope envelope)
	{
		this.envelope = envelope;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

}

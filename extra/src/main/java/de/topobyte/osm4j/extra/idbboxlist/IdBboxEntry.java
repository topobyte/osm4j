package de.topobyte.osm4j.extra.idbboxlist;

import com.vividsolutions.jts.geom.Envelope;

public class IdBboxEntry
{

	private long id;
	private Envelope envelope;

	public IdBboxEntry(long id, Envelope envelope)
	{
		this.id = id;
		this.envelope = envelope;
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

}

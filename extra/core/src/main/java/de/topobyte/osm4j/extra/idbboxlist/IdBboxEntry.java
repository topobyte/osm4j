// Copyright 2020 Sebastian Kuerten
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

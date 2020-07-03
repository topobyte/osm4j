// Copyright 2019 Sebastian Kuerten
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

package de.topobyte.osm4j.osc;

import org.joda.time.DateTime;

public class ReplicationInfo
{

	private DateTime time;
	private long sequenceNumber;

	public ReplicationInfo(DateTime time, long sequenceNumber)
	{
		this.time = time;
		this.sequenceNumber = sequenceNumber;
	}

	public DateTime getTime()
	{
		return time;
	}

	public void setTime(DateTime time)
	{
		this.time = time;
	}

	public long getSequenceNumber()
	{
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

}

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

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ReplicationState
{

	public static ReplicationInfo parse(String content) throws IOException
	{
		Properties props = new Properties();
		props.load(new StringReader(content));

		String sequenceNumber = props.getProperty("sequenceNumber");
		String timestamp = props.getProperty("timestamp");

		long sn = Long.parseLong(sequenceNumber);

		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
		DateTime time = formatter.parseDateTime(timestamp);

		return new ReplicationInfo(time, sn);
	}

}

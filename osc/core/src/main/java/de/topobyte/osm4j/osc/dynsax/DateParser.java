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

package de.topobyte.osm4j.osc.dynsax;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

class DateParser
{

	private static final String[] PATTERNS = { "yyyy-MM-dd'T'HH:mm:ss'Z'",
			"yyyy-MM-dd'T'HH:mm:ssZ" };

	private static final DateTimeFormatter[] PARSERS;

	static {
		PARSERS = new DateTimeFormatter[PATTERNS.length];
		for (int i = 0; i < PATTERNS.length; i++) {
			PARSERS[i] = DateTimeFormat.forPattern(PATTERNS[i]).withZoneUTC();
		}
	}

	private DateTimeFormatter current = PARSERS[0];

	public DateTime parse(String formattedDate)
	{
		try {
			return current.parseDateTime(formattedDate);
		} catch (IllegalArgumentException e) {
			// try other parsers
		}

		for (int i = 0; i < PARSERS.length; i++) {
			DateTimeFormatter parser = PARSERS[i];
			if (parser == current) {
				continue;
			}
			try {
				DateTime result = parser.parseDateTime(formattedDate);
				current = parser;
				return result;
			} catch (IllegalArgumentException e) {
				// continue with next pattern
			}
		}

		throw new RuntimeException(
				"Unable to parse date '" + formattedDate + "'");
	}

}

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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationUtil
{

	final static Logger logger = LoggerFactory.getLogger(ReplicationUtil.class);

	public static ReplicationInfo getMinuteInfo()
			throws MalformedURLException, IOException
	{
		String url = ReplicationFiles.minuteState();

		InputStream input = new URL(url).openConnection().getInputStream();
		String text = IOUtils.toString(input);

		ReplicationInfo info = ReplicationState.parse(text);

		return info;
	}

	public static ReplicationInfo getMinuteInfo(long sequenceNumber)
			throws MalformedURLException, IOException
	{
		String url = ReplicationFiles.minuteState(sequenceNumber);

		InputStream input = new URL(url).openConnection().getInputStream();
		String text = IOUtils.toString(input);

		ReplicationInfo info = ReplicationState.parse(text);

		return info;
	}

	public static ReplicationInfo findMinute(DateTime needle)
			throws MalformedURLException, IOException
	{
		ReplicationInfo last = getMinuteInfo();
		ReplicationInfo first = getMinuteInfo(1);
		return findMinute(last, first, needle);
	}

	public static ReplicationInfo findMinute(ReplicationInfo high,
			ReplicationInfo low, DateTime needle)
			throws MalformedURLException, IOException
	{
		logger.debug("Searching: " + needle);
		logger.debug("Searching between:");
		log(high);
		log(low);

		Minutes minutes = Minutes.minutesBetween(low.getTime(), high.getTime());
		logger.debug("minutes in between: " + minutes.getMinutes());

		while (true) {
			long midNum = (high.getSequenceNumber() + low.getSequenceNumber())
					/ 2;
			ReplicationInfo mid = getMinuteInfo(midNum);

			if (needle.isBefore(mid.getTime())) {
				high = mid;
			} else {
				low = mid;
			}
			log(high);
			log(low);
			if (high.getSequenceNumber() - low.getSequenceNumber() < 2) {
				break;
			}
		}

		return low;
	}

	private static void log(ReplicationInfo info)
	{
		logger.debug(String.format("%s: %d", info.getTime(),
				info.getSequenceNumber()));
	}

}

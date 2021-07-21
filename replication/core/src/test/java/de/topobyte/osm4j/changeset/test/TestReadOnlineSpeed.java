// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.changeset.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import de.topobyte.osm4j.changeset.OsmChangeset;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsHandler;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsReader;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.replication.ReplicationFiles;

public class TestReadOnlineSpeed implements OsmChangesetsHandler
{

	private CloseableHttpClient httpclient = HttpClients.createDefault();

	private int numFiles = 0;
	private int numChangesets = 0;
	private int numChanges = 0;

	public static void main(String[] args) throws IOException, OsmInputException
	{
		TestReadOnlineSpeed task = new TestReadOnlineSpeed();
		task.test();
	}

	public void test() throws IOException, OsmInputException
	{
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1440; i++) {
			long before = System.currentTimeMillis();
			fetchAndParse(4538273 + i);
			long now = System.currentTimeMillis();
			long thisDuration = now - before;
			long totalDuration = now - start;
			long perFile = totalDuration / (i + 1);
			System.out.println(String.format(
					"total: %s, this: %s, per file: %s", format(totalDuration),
					format(thisDuration), format(perFile)));
		}
	}

	private Object format(long millis)
	{
		if (millis < 1000) {
			return String.format("%dms", millis);
		}
		double seconds = millis / 1000d;
		if (seconds < 60) {
			return String.format("%.1fs", seconds);
		}
		double minutes = seconds / 60;
		return String.format("%.1fmin", minutes);
	}

	private void fetchAndParse(int sequence)
			throws ClientProtocolException, IOException, OsmInputException
	{
		numFiles += 1;

		String url = ReplicationFiles.changesets(sequence);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(get);
		HttpEntity entity = response.getEntity();

		InputStream cinput = entity.getContent();
		InputStream input = new GzipCompressorInputStream(cinput);

		OsmChangesetsReader reader = new OsmChangesetsReader(input);
		reader.setHandler(this);
		reader.read();

		System.out
				.println(String.format("files: %d, changesets: %d, changes: %d",
						numFiles, numChangesets, numChanges));
	}

	@Override
	public void handle(OsmChangeset changeset) throws IOException
	{
		numChangesets += 1;
		numChanges += changeset.getNumChanges();
	}

	@Override
	public void complete() throws IOException
	{
		// do nothing
	}

}

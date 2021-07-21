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
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.changeset.ChangesetUtil;
import de.topobyte.osm4j.changeset.OsmChangeset;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsHandler;
import de.topobyte.osm4j.changeset.dynsax.OsmChangesetsReader;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.replication.ReplicationFiles;

public class TestGatherEditorStats implements OsmChangesetsHandler
{

	private CloseableHttpClient httpclient = HttpClients.createDefault();

	private MultiSet<String> editors = new HashMultiSet<>();

	public static void main(String[] args) throws IOException, OsmInputException
	{
		TestGatherEditorStats task = new TestGatherEditorStats();
		task.test();
	}

	public void test() throws IOException, OsmInputException
	{
		for (int i = 0; i < 600; i++) {
			fetchAndParse(4538273 + i);
		}
		for (String key : editors.keySet()) {
			int count = editors.occurences(key);
			System.out.println(String.format("%d: %s", count, key));
		}
	}

	private void fetchAndParse(int sequence)
			throws ClientProtocolException, IOException, OsmInputException
	{
		String url = ReplicationFiles.changesets(sequence);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(get);
		HttpEntity entity = response.getEntity();

		InputStream cinput = entity.getContent();
		InputStream input = new GzipCompressorInputStream(cinput);

		OsmChangesetsReader reader = new OsmChangesetsReader(input);
		reader.setHandler(this);
		reader.read();
	}

	@Override
	public void handle(OsmChangeset changeset) throws IOException
	{
		Map<String, String> tags = ChangesetUtil.getTagsAsMap(changeset);
		String createdBy = tags.get("created_by");
		if (createdBy == null) {
			return;
		}
		editors.add(createdBy);
	}

	@Override
	public void complete() throws IOException
	{
		// do nothing
	}

}

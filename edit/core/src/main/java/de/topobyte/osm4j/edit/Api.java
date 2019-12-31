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

package de.topobyte.osm4j.edit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import de.topobyte.osm4j.core.model.iface.OsmTag;

public class Api
{

	private String user;
	private String pass;

	public Api(String user, String pass)
	{
		this.user = user;
		this.pass = pass;
	}

	private static final String SCHEME = "https";
	private static final String ENDPOINT = "master.apis.dev.openstreetmap.org";

	private static URIBuilder builder()
	{
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(SCHEME);
		uriBuilder.setHost(ENDPOINT);
		return uriBuilder;
	}

	private CloseableHttpResponse executeForResponse(URIBuilder builder,
			String payload)
			throws URISyntaxException, ClientProtocolException, IOException
	{
		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				user, pass);
		provider.setCredentials(AuthScope.ANY, credentials);

		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(provider).build();

		HttpPut request = new HttpPut(builder.build());
		request.setEntity(new StringEntity(payload));

		CloseableHttpResponse response = client.execute(request);
		return response;
	}

	private String executeForString(URIBuilder builder, String payload)
			throws URISyntaxException, ClientProtocolException, IOException
	{
		CloseableHttpResponse response = executeForResponse(builder, payload);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);
		return responseText;
	}

	private StatusLine executeForStatus(URIBuilder builder, String payload)
			throws URISyntaxException, ClientProtocolException, IOException
	{
		CloseableHttpResponse response = executeForResponse(builder, payload);
		return response.getStatusLine();
	}

	public Changeset createChangeset() throws URISyntaxException,
			ClientProtocolException, IOException, ParserConfigurationException
	{
		Document document = Documents.createChangeset();
		String payload = Documents.toString(document);

		URIBuilder builder = builder();
		builder.setPath("/api/0.6/changeset/create");

		String responseText = executeForString(builder, payload);

		long id = Long.parseLong(responseText);
		return new Changeset(id);
	}

	public boolean closeChangeset(Changeset changeset)
			throws URISyntaxException, ClientProtocolException, IOException,
			ParserConfigurationException
	{
		Document document = Documents.createChangeset();
		String payload = Documents.toString(document);

		URIBuilder builder = builder();
		builder.setPath(String.format("/api/0.6/changeset/%d/close",
				changeset.getId()));

		StatusLine status = executeForStatus(builder, payload);
		return status.getStatusCode() == HttpStatus.SC_OK;
	}

	public long createNode(Changeset changeset, double lon, double lat,
			List<? extends OsmTag> tags)
			throws ParserConfigurationException, IOException, URISyntaxException
	{
		Document document = Documents.createNode(changeset, lon, lat, tags);
		String payload = Documents.toString(document);

		URIBuilder builder = builder();
		builder.setPath("/api/0.6/node/create");

		String responseText = executeForString(builder, payload);

		long id = Long.parseLong(responseText);
		return id;
	}

}

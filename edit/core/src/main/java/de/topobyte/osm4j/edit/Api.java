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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
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

	public Changeset createChangeset() throws URISyntaxException,
			ClientProtocolException, IOException, ParserConfigurationException
	{
		Document document = Documents.createChangeset();
		String payload = Documents.toString(document);

		URIBuilder builder = builder();
		builder.setPath("/api/0.6/changeset/create");

		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
				user, pass);
		provider.setCredentials(AuthScope.ANY, credentials);

		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(provider).build();

		HttpPut request = new HttpPut(builder.build());
		request.setEntity(new StringEntity(payload));

		CloseableHttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);

		long id = Long.parseLong(responseText);
		return new Changeset(id);
	}

}

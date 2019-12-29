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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class TestCreateNode
{

	public static void main(String[] args)
			throws IOException, URISyntaxException
	{
		Properties properties = new Properties();
		try (InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("secure.properties")) {
			properties.load(input);
		}

		String user = (String) properties.get("user");
		String pass = (String) properties.get("pass");

		String payload = "<osm><changeset><tag k=\"created_by\" v=\"test 1.0\"/></changeset></osm>";

		URIBuilder builder = builder();

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
		System.out.println(responseText);
	}

	private static final String SCHEME = "https";
	private static final String ENDPOINT = "master.apis.dev.openstreetmap.org";

	private static URIBuilder builder()
	{
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(SCHEME);
		uriBuilder.setHost(ENDPOINT);
		uriBuilder.setPath("/api/0.6/changeset/create");
		return uriBuilder;
	}

}

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

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * The default HttpDelete implementation does not allow a request body.
 */
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
{

	public final static String METHOD_NAME = "DELETE";

	public HttpDeleteWithBody()
	{
		super();
	}

	public HttpDeleteWithBody(final URI uri)
	{
		super();
		setURI(uri);
	}

	public HttpDeleteWithBody(final String uri)
	{
		super();
		setURI(URI.create(uri));
	}

	@Override
	public String getMethod()
	{
		return METHOD_NAME;
	}

}
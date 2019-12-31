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

		Api api = new Api(user, pass);
		Changeset changeset = api.createChangeset();
		System.out.println(changeset.getId());
	}

}

// Copyright 2015 Sebastian Kuerten
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

package de.topobyte.osm4j.pbf;

import java.io.IOException;

public class TestReadWrite
{

	public static void main(String[] args) throws IOException
	{
		String resource1 = "data-with-metadata.pbf";
		String resource2 = "data-without-metadata.pbf";

		Util.copyAndRead(resource1, true, true);
		Util.copyAndRead(resource1, true, false);
		Util.copyAndRead(resource1, false, true);
		Util.copyAndRead(resource1, false, false);

		Util.copyAndRead(resource2, true, true);
		Util.copyAndRead(resource2, true, false);
		Util.copyAndRead(resource2, false, true);
		Util.copyAndRead(resource2, false, false);
	}

}

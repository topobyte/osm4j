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

package de.topobyte.osm4j.replication.test.changesets;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import de.topobyte.osm4j.replication.ChangesetReplicationState;
import de.topobyte.osm4j.replication.ReplicationFiles;
import de.topobyte.osm4j.replication.ReplicationInfo;

public class TestChangesetStateLatest
{

	@Test
	public void test() throws IOException
	{
		String url = ReplicationFiles.changesetsState();

		InputStream input = new URL(url).openConnection().getInputStream();
		String text = IOUtils.toString(input, StandardCharsets.UTF_8);
		System.out.println(text);

		ReplicationInfo info = ChangesetReplicationState.parse(text);
		System.out.println(String.format("%d: %s", info.getSequenceNumber(),
				info.getTime()));
	}

}

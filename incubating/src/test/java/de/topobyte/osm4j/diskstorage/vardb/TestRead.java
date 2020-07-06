// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.vardb;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for vardb in general. Corresponds to TestWrite.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestRead
{

	final static Logger logger = LoggerFactory.getLogger(TestRead.class);

	/**
	 * Find an entry of a test database by a given id.
	 * 
	 * @param args
	 *            none
	 * @throws IOException
	 *             on failure
	 */
	public static void main(String[] args) throws IOException
	{
		Path fileDB = Paths.get("/tmp/vardb.dat");
		Path fileIndex = Paths.get("/tmp/vardb.idx");

		VarDB<MyRecord> varDB = new VarDB<>(fileDB, fileIndex,
				new MyRecord(0, 0));

		Record find = varDB.find(2);
		logger.debug("found: " + find);

		varDB.close();
	}

}

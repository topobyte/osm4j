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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.topobyte.osm4j.diskstorage.HighLevelOutputStream;
import de.topobyte.osm4j.diskstorage.vardb.Record;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;

/**
 * Test class for vardb in general. Corresponds to TestRead.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class TestWrite
{

	/**
	 * Write some test records to files. Don't forget to delete them before.
	 * 
	 * @param args
	 *            none
	 * @throws IOException
	 *             on io failure
	 */
	public static void main(String[] args) throws IOException
	{
		Path fileDB = Paths.get("/tmp/vardb.data");
		Path fileIndex = Paths.get("/tmp/vardb.index");

		VarDB<MyRecord> varDB = new VarDB<>(fileDB, fileIndex,
				new MyRecord(0, 0));

		varDB.addRecord(new MyRecord(1, 16));
		varDB.addRecord(new MyRecord(2, 32));
		varDB.addRecord(new MyRecord(3, 32));
		varDB.addRecord(new MyRecord(4, 4));
		varDB.addRecord(new MyRecord(5, 4));
		varDB.addRecord(new MyRecord(6, 4));
		varDB.close();

	}

}

class MyRecord extends Record
{

	long id;
	int nbytes;

	public MyRecord(long id, int nbytes)
	{
		this.id = id;
		this.nbytes = nbytes;
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public int getNumberOfBytes()
	{
		return nbytes;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException
	{
		HighLevelOutputStream hlos = new HighLevelOutputStream(stream);
		for (int i = 0; i < nbytes; i++) {
			hlos.writeByte(0xff);
		}
		hlos.close();
	}

	@Override
	public Record fromBytes(long recordId, InputStream stream, int recordBytes)
	{
		return new MyRecord(recordId, nbytes);
	}

}

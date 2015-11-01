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

package de.topobyte.osm4j.testing;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class PrintSomeGeneratedData
{

	public static void main(String[] args) throws IOException
	{
		EntityGenerator entityGenerator = new EntityGenerator(100, true);
		DataSetGenerator dataSetGenerator = new DataSetGenerator(
				entityGenerator);
		DataSet dataSet = dataSetGenerator.generate(10, 3, 2);

		OsmOutputStream output = new OsmXmlOutputStream(System.out, true);

		DataSetHelper.write(dataSet, output);

		output.complete();
	}

}

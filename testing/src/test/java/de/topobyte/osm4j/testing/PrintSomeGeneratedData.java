package de.topobyte.osm4j.testing;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.testing.DataSet;
import de.topobyte.osm4j.testing.DataSetGenerator;
import de.topobyte.osm4j.testing.DataSetHelper;
import de.topobyte.osm4j.testing.EntityGenerator;
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

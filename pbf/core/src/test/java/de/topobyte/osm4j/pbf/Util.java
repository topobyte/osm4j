package de.topobyte.osm4j.pbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.access.PbfIterator;
import de.topobyte.osm4j.pbf.access.PbfWriter;

public class Util
{

	public static void iterate(String resource, boolean fetchMetadata)
			throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		OsmIterator iterator = new PbfIterator(input, fetchMetadata);

		while (iterator.hasNext()) {
			iterator.next();
		}

		input.close();
	}

	public static void iterate(File file, boolean fetchMetadata)
			throws IOException
	{
		InputStream input = new FileInputStream(file);

		OsmIterator iterator = new PbfIterator(input, fetchMetadata);

		while (iterator.hasNext()) {
			iterator.next();
		}

		input.close();
	}

	public static void copyAndRead(String resource, boolean readMetadata,
			boolean writeMetadata) throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		OsmIterator iterator = new PbfIterator(input, readMetadata);

		File file = File.createTempFile("osm4j-test", "pbf");
		OutputStream output = new FileOutputStream(file);
		OsmOutputStream osmOutput = new PbfWriter(output, writeMetadata);

		for (EntityContainer container : iterator) {
			switch (container.getType()) {
			default:
			case Node:
				osmOutput.write((OsmNode) container.getEntity());
				break;
			case Way:
				osmOutput.write((OsmWay) container.getEntity());
				break;
			case Relation:
				osmOutput.write((OsmRelation) container.getEntity());
				break;
			}
		}

		osmOutput.complete();
		output.close();

		Util.iterate(file, true);
		Util.iterate(file, false);

		file.delete();
	}

}

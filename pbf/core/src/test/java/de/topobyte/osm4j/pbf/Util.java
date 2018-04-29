package de.topobyte.osm4j.pbf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.DefaultOsmHandler;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.pbf.seq.PbfParser;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

public class Util
{

	public static OsmIterator iterator(String resource, boolean fetchMetadata)
			throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		OsmIterator iterator = new PbfIterator(input, fetchMetadata);

		return iterator;
	}

	public static OsmIterator xmlIterator(String resource,
			boolean fetchMetadata) throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		OsmIterator iterator = new OsmXmlIterator(input, fetchMetadata);

		return iterator;
	}

	public static PbfReader reader(String resource, boolean fetchMetadata)
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		PbfReader parser = new PbfReader(input, fetchMetadata);
		return parser;
	}

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

	private static OsmHandler nullHandler = new DefaultOsmHandler() {
		// do nothing
	};

	public static void read(String resource, boolean fetchMetadata)
			throws IOException
	{
		InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource);

		PbfParser parser = new PbfParser(nullHandler, fetchMetadata);
		parser.parse(input);

		input.close();
	}

	public static void read(File file, boolean fetchMetadata) throws IOException
	{
		InputStream input = new FileInputStream(file);

		PbfParser parser = new PbfParser(nullHandler, fetchMetadata);
		parser.parse(input);

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

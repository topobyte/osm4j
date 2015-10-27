package de.topobyte.osm4j.extra.datatree;

import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class Util
{

	public static OsmIterator setupOsmInput(InputStream in, FileFormat format,
			boolean readMetadata)
	{
		switch (format) {
		default:
		case TBO:
			return new TboIterator(in);
		case XML:
			return new OsmXmlIterator(in, readMetadata);
		case PBF:
			return new PbfIterator(in, readMetadata);
		}
	}

	public static OsmOutputStream setupOsmOutput(OutputStream out,
			FileFormat format, boolean writeMetadata, PbfConfig pbfConfig)
	{
		switch (format) {
		default:
		case TBO:
			return new TboWriter(out);
		case XML:
			return new OsmXmlOutputStream(out, writeMetadata);
		case PBF:
			PbfWriter pbfWriter = new PbfWriter(out, writeMetadata);
			pbfWriter.setCompression(pbfConfig.getCompression());
			pbfWriter.setUseDense(pbfConfig.isUseDenseNodes());
			return pbfWriter;
		}
	}

}

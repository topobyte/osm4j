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

package de.topobyte.osm4j.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmElementCounter;
import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.access.OsmIdReader;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.access.wrapper.OsmElementCounterReaderAdapter;
import de.topobyte.osm4j.core.access.wrapper.OsmIdIteratorAdapter;
import de.topobyte.osm4j.core.access.wrapper.OsmIdReaderAdapter;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import de.topobyte.osm4j.pbf.seq.PbfWriter;
import de.topobyte.osm4j.tbo.access.TboIdIterator;
import de.topobyte.osm4j.tbo.access.TboIdReader;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.tbo.access.TboReader;
import de.topobyte.osm4j.tbo.access.TboWriter;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;
import de.topobyte.osm4j.xml.output.OsmXmlOutputStream;

public class OsmIoUtils
{

	public static OsmIterator setupOsmIterator(InputStream in,
			FileFormat format, boolean readMetadata) throws IOException
	{
		return setupOsmIterator(in, format, true, readMetadata);
	}

	public static OsmIterator setupOsmIterator(InputStream in,
			FileFormat format, boolean readTags, boolean readMetadata)
			throws IOException
	{
		switch (format) {
		default:
		case PBF:
			return new PbfIterator(in, readMetadata);
		case TBO:
			return new TboIterator(in, readTags, readMetadata);
		case XML:
			return new OsmXmlIterator(in, readMetadata);
		}
	}

	public static OsmReader setupOsmReader(InputStream in, FileFormat format,
			boolean readMetadata) throws IOException
	{
		return setupOsmReader(in, format, true, readMetadata);
	}

	public static OsmReader setupOsmReader(InputStream in, FileFormat format,
			boolean readTags, boolean readMetadata) throws IOException
	{
		switch (format) {
		default:
		case PBF:
			return new PbfReader(in, readMetadata);
		case TBO:
			return new TboReader(in, readTags, readMetadata);
		case XML:
			return new OsmXmlReader(in, readMetadata);
		}
	}

	public static OsmIdIterator setupOsmIdIterator(InputStream in,
			FileFormat format) throws IOException
	{
		switch (format) {
		default:
		case PBF:
			OsmIterator pbfIterator = new PbfIterator(in, false);
			return new OsmIdIteratorAdapter(pbfIterator);
		case TBO:
			return new TboIdIterator(in);
		case XML:
			OsmIterator xmlIterator = new OsmXmlIterator(in, false);
			return new OsmIdIteratorAdapter(xmlIterator);
		}
	}

	public static OsmIdReader setupOsmIdReader(InputStream in, FileFormat format)
			throws IOException
	{
		switch (format) {
		default:
		case PBF:
			OsmReader pbfReader = new PbfReader(in, false);
			return new OsmIdReaderAdapter(pbfReader);
		case TBO:
			return new TboIdReader(in);
		case XML:
			OsmReader xmlReader = new OsmXmlReader(in, false);
			return new OsmIdReaderAdapter(xmlReader);
		}
	}

	public static OsmElementCounter setupOsmElementCounter(InputStream in,
			FileFormat format) throws IOException
	{
		switch (format) {
		default:
		case PBF:
			OsmReader pbfReader = new PbfReader(in, false);
			return new OsmElementCounterReaderAdapter(pbfReader);
		case TBO:
			TboReader tboReader = new TboReader(in, false, false);
			return new OsmElementCounterReaderAdapter(tboReader);
		case XML:
			OsmReader xmlReader = new OsmXmlReader(in, false);
			return new OsmElementCounterReaderAdapter(xmlReader);
		}
	}

	public static OsmOutputStream setupOsmOutput(OutputStream out,
			FileFormat format, boolean writeMetadata, PbfConfig pbfConfig,
			TboConfig tboConfig)
	{
		switch (format) {
		default:
		case PBF:
			PbfWriter pbfWriter = new PbfWriter(out, writeMetadata);
			pbfWriter.setCompression(pbfConfig.getCompression());
			pbfWriter.setUseDense(pbfConfig.isUseDenseNodes());
			return pbfWriter;
		case TBO:
			TboWriter tboWriter = new TboWriter(out, writeMetadata, true);
			tboWriter.setCompression(tboConfig.getCompression());
			return tboWriter;
		case XML:
			return new OsmXmlOutputStream(out, writeMetadata);
		}
	}

	public static OsmOutputStream setupOsmOutput(OutputStream out,
			OsmOutputConfig outputConfig)
	{
		boolean writeMetadata = outputConfig.isWriteMetadata();

		switch (outputConfig.getFileFormat()) {
		default:
		case PBF:
			PbfConfig pbfConfig = outputConfig.getPbfConfig();
			PbfWriter pbfWriter = new PbfWriter(out, writeMetadata);
			pbfWriter.setCompression(pbfConfig.getCompression());
			pbfWriter.setUseDense(pbfConfig.isUseDenseNodes());
			return pbfWriter;
		case TBO:
			TboConfig tboConfig = outputConfig.getTboConfig();
			TboWriter tboWriter = new TboWriter(out, writeMetadata, true);
			tboWriter.setCompression(tboConfig.getCompression());
			return tboWriter;
		case XML:
			return new OsmXmlOutputStream(out, writeMetadata);
		}
	}

	public static String extension(FileFormat format)
	{
		switch (format) {
		default:
			return null;
		case PBF:
			return ".pbf";
		case TBO:
			return ".tbo";
		case XML:
			return ".xml";
		}
	}

}

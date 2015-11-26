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

package de.topobyte.osm4j.extra.datatree.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class ClosingDataTreeOutputFactory implements DataTreeOutputFactory
{

	private DataTreeFiles treeFiles;
	private OsmOutputConfig outputConfig;

	private ClosingFileOutputStreamFactory outputStreamFactory;

	public ClosingDataTreeOutputFactory(DataTreeFiles treeFiles,
			OsmOutputConfig outputConfig)
	{
		this.treeFiles = treeFiles;
		this.outputConfig = outputConfig;
		this.outputStreamFactory = new SimpleClosingFileOutputStreamFactory();
	}

	@Override
	public OsmStreamOutput init(Node leaf, boolean writeBounds)
			throws IOException
	{
		Path file = treeFiles.getPath(leaf);
		Path dir = treeFiles.getSubdirPath(leaf);
		Files.createDirectories(dir);

		OutputStream os = outputStreamFactory.create(file.toFile());
		OutputStream bos = new BufferedOutputStream(os);
		OsmOutputStream osmOutput = OsmIoUtils
				.setupOsmOutput(bos, outputConfig);
		OsmStreamOutput output = new OsmOutputStreamStreamOutput(bos, osmOutput);

		if (writeBounds) {
			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}

		return output;
	}

}

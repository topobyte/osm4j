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

package de.topobyte.osm4j.extra.datatree.ways;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class SimpleWaysToTreeMapper extends AbstractWaysToTreeMapper
{

	private String fileNamesOutput;
	private OsmOutputConfig outputConfig;

	public SimpleWaysToTreeMapper(OsmIterator nodeIterator, Path pathTree,
			Path pathWays, FileFormat inputFormatWays, String fileNamesOutput,
			OsmOutputConfig outputConfig)
	{
		super(nodeIterator, pathTree, pathWays, inputFormatWays, outputConfig
				.isWriteMetadata());
		this.fileNamesOutput = fileNamesOutput;
		this.outputConfig = outputConfig;
	}

	private DataTree tree;

	private Map<Node, OsmStreamOutput> outputs = new HashMap<>();

	@Override
	public void prepare() throws IOException
	{
		super.prepare();

		DataTreeFiles filesOutput = new DataTreeFiles(pathTree, fileNamesOutput);

		List<Node> leafs = tree.getLeafs();

		// Node outputs
		ClosingFileOutputStreamFactory factoryOut = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : leafs) {
			File fileOutput = filesOutput.getFile(leaf);
			OutputStream output = factoryOut.create(fileOutput);
			output = new BufferedOutputStream(output);
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputConfig, true);

			OsmStreamOutput out = new OsmOutputStreamStreamOutput(output,
					osmOutput);
			outputs.put(leaf, out);
		}

	}

	@Override
	public void execute() throws IOException
	{
		super.execute();

		for (OsmStreamOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

	@Override
	protected void process(OsmWay way, Node leaf) throws IOException
	{
		OsmStreamOutput output = outputs.get(leaf);
		output.getOsmOutput().write(way);
	}

}

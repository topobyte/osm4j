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

package de.topobyte.osm4j.extra.extracts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.extra.datatree.nodetree.NodeTreeCreatorMaxNodes;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.split.EntitySplitter;

public class ExtractionFilesBuilder
{

	private static final int SPLIT_INITIAL = 20;
	private static final int SPLIT_ITERATION = 8;

	private Path pathInput;
	private FileFormat inputFormat;
	private Path pathOutput;
	private OsmOutputConfig outputConfig;
	private int maxNodes;

	private Path pathTree;

	private Path pathNodes;
	private Path pathWays;
	private Path pathRelations;

	public ExtractionFilesBuilder(Path pathInput, FileFormat inputFormat,
			Path pathOutput, OsmOutputConfig outputConfig, int maxNodes)
	{
		this.pathInput = pathInput;
		this.inputFormat = inputFormat;
		this.pathOutput = pathOutput;
		this.outputConfig = outputConfig;
		this.maxNodes = maxNodes;
	}

	public void execute() throws IOException
	{
		System.out.println("Output directory: " + pathOutput);
		Files.createDirectories(pathOutput);
		if (!Files.isDirectory(pathOutput)) {
			System.out.println("Unable to create output directory");
			System.exit(1);
		}
		if (pathOutput.toFile().listFiles().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}

		pathNodes = pathOutput.resolve("nodes"
				+ OsmIoUtils.extension(outputConfig.getFileFormat()));
		pathWays = pathOutput.resolve("ways"
				+ OsmIoUtils.extension(outputConfig.getFileFormat()));
		pathRelations = pathOutput.resolve("relations"
				+ OsmIoUtils.extension(outputConfig.getFileFormat()));

		pathTree = pathOutput.resolve("tree");

		OsmFileInput fileInput = new OsmFileInput(pathInput, inputFormat);

		OsmFileInput fileInputNodes = new OsmFileInput(pathNodes,
				outputConfig.getFileFormat());
		OsmFileInput fileInputWays = new OsmFileInput(pathWays,
				outputConfig.getFileFormat());
		OsmFileInput fileInputRelations = new OsmFileInput(pathRelations,
				outputConfig.getFileFormat());

		String fileNamesNodes = "nodes"
				+ OsmIoUtils.extension(outputConfig.getFileFormat());

		// Split entities

		OsmIteratorInput input = fileInput.createIterator(outputConfig
				.isWriteMetadata());

		EntitySplitter splitter = new EntitySplitter(input.getIterator(),
				pathNodes, pathWays, pathRelations);
		splitter.execute();

		input.close();

		// Create node tree

		NodeTreeCreatorMaxNodes creator = new NodeTreeCreatorMaxNodes(
				fileInputNodes, maxNodes, SPLIT_INITIAL, SPLIT_ITERATION,
				pathTree, fileNamesNodes, outputConfig);

		creator.init();
		creator.buildTree();
	}

}

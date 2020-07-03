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

package de.topobyte.osm4j.extra.relations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Envelope;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxListOutputStream;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public abstract class RelationsDistributorBase
{

	protected Path pathTree;
	protected Path pathData;
	protected Path pathOutputEmpty;
	protected Path pathOutputNonTree;
	protected Path pathOutputBboxes;

	protected String fileNamesRelations;
	protected String fileNamesWays;
	protected String fileNamesNodes;
	protected String fileNamesTreeRelations;

	protected FileFormat inputFormat;
	protected OsmOutputConfig outputConfig;

	protected DataTree tree;
	protected List<Path> subdirs;

	protected DataTreeFiles treeFilesRelations;

	protected OsmStreamOutput outputEmpty;
	protected OsmStreamOutput outputNonTree;
	protected Map<Node, OsmStreamOutput> outputs = new HashMap<>();

	protected IdBboxListOutputStream outputBboxes;

	public RelationsDistributorBase(Path pathTree, Path pathData,
			Path pathOutputEmpty, Path pathOutputNonTree,
			Path pathOutputBboxes, String fileNamesRelations,
			String fileNamesWays, String fileNamesNodes,
			String fileNamesTreeRelations, FileFormat inputFormat,
			OsmOutputConfig outputConfig)
	{
		this.pathTree = pathTree;
		this.pathData = pathData;
		this.pathOutputEmpty = pathOutputEmpty;
		this.pathOutputNonTree = pathOutputNonTree;
		this.pathOutputBboxes = pathOutputBboxes;
		this.fileNamesRelations = fileNamesRelations;
		this.fileNamesWays = fileNamesWays;
		this.fileNamesNodes = fileNamesNodes;
		this.fileNamesTreeRelations = fileNamesTreeRelations;
		this.inputFormat = inputFormat;
		this.outputConfig = outputConfig;
	}

	protected void init() throws IOException
	{
		if (!Files.isDirectory(pathData)) {
			System.out.println("Input path is not a directory");
			System.exit(1);
		}

		tree = DataTreeOpener.open(pathTree.toFile());

		treeFilesRelations = new DataTreeFiles(pathTree, fileNamesTreeRelations);

		subdirs = new ArrayList<>();
		File[] subs = pathData.toFile().listFiles();
		for (File sub : subs) {
			if (!sub.isDirectory()) {
				continue;
			}
			Path subPath = sub.toPath();
			Path relations = subPath.resolve(fileNamesRelations);
			Path ways = subPath.resolve(fileNamesWays);
			Path nodes = subPath.resolve(fileNamesNodes);
			if (!Files.exists(relations) || !Files.exists(ways)
					|| !Files.exists(nodes)) {
				continue;
			}
			subdirs.add(subPath);
		}

		Collections.sort(subdirs, new Comparator<Path>() {

			@Override
			public int compare(Path o1, Path o2)
			{
				String name1 = o1.getFileName().toString();
				String name2 = o2.getFileName().toString();
				try {
					int n1 = Integer.parseInt(name1);
					int n2 = Integer.parseInt(name2);
					return Integer.compare(n1, n2);
				} catch (NumberFormatException e) {
					// compare as paths
				}
				return o1.compareTo(o2);
			}
		});

		// Setup output for non-geometry relations

		OutputStream outEmpty = StreamUtil
				.bufferedOutputStream(pathOutputEmpty);
		OsmOutputStream osmOutputEmpty = OsmIoUtils.setupOsmOutput(outEmpty,
				outputConfig);
		outputEmpty = new OsmOutputStreamStreamOutput(outEmpty, osmOutputEmpty);

		// Setup output for non-tree relations

		OutputStream outNonTree = StreamUtil
				.bufferedOutputStream(pathOutputNonTree);
		OsmOutputStream osmOutputNonTree = OsmIoUtils.setupOsmOutput(
				outNonTree, outputConfig);
		outputNonTree = new OsmOutputStreamStreamOutput(outNonTree,
				osmOutputNonTree);

		// Setup output for non-tree relations' bboxes

		OutputStream outBboxes = StreamUtil
				.bufferedOutputStream(pathOutputBboxes);
		outputBboxes = new IdBboxListOutputStream(outBboxes);

		// Setup output for tree relations

		ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

		for (Node leaf : tree.getLeafs()) {
			File file = treeFilesRelations.getFile(leaf);
			OutputStream out = new BufferedOutputStream(factory.create(file));
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(out,
					outputConfig, true);

			outputs.put(leaf, new OsmOutputStreamStreamOutput(out, osmOutput));

			Envelope box = leaf.getEnvelope();
			osmOutput.write(new Bounds(box.getMinX(), box.getMaxX(), box
					.getMaxY(), box.getMinY()));
		}
	}

	int nWrittenEmpty = 0;
	int nWrittenToTree = 0;
	int nRemaining = 0;

	protected void run() throws IOException
	{
		int i = 0;
		for (Path path : subdirs) {
			System.out.println(String.format("Processing directory %d of %d",
					++i, subdirs.size()));
			build(path);
			System.out.println(String.format(
					"empty: %d, tree: %d, remaining: %d", nWrittenEmpty,
					nWrittenToTree, nRemaining));
		}
	}

	protected void finish() throws IOException
	{
		outputEmpty.getOsmOutput().complete();
		outputEmpty.close();

		outputNonTree.getOsmOutput().complete();
		outputNonTree.close();

		outputBboxes.close();

		for (OsmStreamOutput output : outputs.values()) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

	protected abstract void build(Path path) throws IOException;

	protected InMemoryMapDataSet read(Path path, boolean readMetadata,
			boolean keepTags) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, readMetadata);
		InMemoryMapDataSet data = MapDataSetLoader.read(osmIterator, keepTags,
				keepTags, keepTags);
		input.close();
		return data;
	}

}
